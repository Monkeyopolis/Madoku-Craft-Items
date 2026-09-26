package madoku.craft.java.items;

import madoku.craft.java.core.rarity.RarityAPIManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Dedicated synchronized menu for item-level upgrades. */
public final class ItemMenu extends AbstractContainerMenu {
	public static final int UPGRADE_SLOT_START = 0;
	public static final int UPGRADE_SLOT_COUNT = 4;
	public static final int PLAYER_INVENTORY_START = UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT;
	public static final int HOTBAR_START = PLAYER_INVENTORY_START + 27;
	public static final int MENU_SLOT_COUNT = HOTBAR_START + 9;
	public static final int TARGET_SLOT_X = 80;
	public static final int TARGET_SLOT_Y = 26;
	public static final int ITEM_REQUIREMENT_SLOT_X = 54;
	public static final int EXPERIENCE_REQUIREMENT_SLOT_X = 80;
	public static final int ESSENCE_REQUIREMENT_SLOT_X = 106;
	public static final int REQUIREMENT_SLOT_Y = 74;
	private static final int EXPERIENCE_MATERIAL_STEP = 4;

	private final SimpleContainer upgradeInventory = new SimpleContainer(UPGRADE_SLOT_COUNT);

	public ItemMenu(int containerId, Inventory playerInventory) {
		super(ItemMenuManager.ITEM_MENU, containerId);
		this.addSlot(new Slot(upgradeInventory, 0, TARGET_SLOT_X, TARGET_SLOT_Y) {
			@Override public boolean mayPlace(ItemStack stack) { return isValidItem(stack); }
			@Override public int getMaxStackSize() { return 1; }
		});
		this.addSlot(displayOnlySlot(1, ITEM_REQUIREMENT_SLOT_X, REQUIREMENT_SLOT_Y));
		this.addSlot(displayOnlySlot(2, EXPERIENCE_REQUIREMENT_SLOT_X, REQUIREMENT_SLOT_Y));
		this.addSlot(displayOnlySlot(3, ESSENCE_REQUIREMENT_SLOT_X, REQUIREMENT_SLOT_Y));

		for (int inventoryIndex = 9; inventoryIndex < 36; inventoryIndex++) {
			int slot = inventoryIndex - 9;
			this.addSlot(new Slot(playerInventory, inventoryIndex, 8 + slot % 9 * 18, 134 + slot / 9 * 18));
		}
		for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
			this.addSlot(new Slot(playerInventory, hotbarIndex, 8 + hotbarIndex * 18, 192));
		}
	}

	public UpgradeRequirements getUpgradeRequirements() {
		ItemStack target = upgradeInventory.getItem(UPGRADE_SLOT_START);
		if (!isValidItem(target)) return UpgradeRequirements.empty();

		int level = itemLevel(target);
		UpgradeRequirement itemCopies = new UpgradeRequirement(countItemCopies(target), level);
		int experienceCost = experienceBottleCost(target, level);
		UpgradeRequirement experienceBottles = new UpgradeRequirement(countItems(Items.EXPERIENCE_BOTTLE), experienceCost);
		UpgradeRequirement essence = new UpgradeRequirement(countItems(EssenceManager.ESSENCE), experienceCost * 2);
		boolean belowMaximum = level < ItemsCategoriesAPIManager.getItemMaximumLevel();
		boolean canUpgrade = belowMaximum && itemCopies.isMet() && experienceBottles.isMet() && essence.isMet();
		return new UpgradeRequirements(true, canUpgrade, itemCopies, experienceBottles, essence);
	}

	/** Applies an upgrade on the server after rechecking all costs against the active player's inventory. */
	public boolean upgrade(ServerPlayer player) {
		if (player == null || player.containerMenu != this) return false;

		UpgradeRequirements requirements = getUpgradeRequirements();
		if (!requirements.canUpgrade()) return false;

		ItemStack target = upgradeInventory.getItem(UPGRADE_SLOT_START);
		int nextLevel = itemLevel(target) + 1;
		consumeItemCopies(target, requirements.itemCopies().required());
		consumeItems(Items.EXPERIENCE_BOTTLE, requirements.experienceBottles().required());
		consumeItems(EssenceManager.ESSENCE, requirements.essence().required());
		ItemsCategoriesAPIManager.setItemLevel(target, nextLevel);
		upgradeInventory.setChanged();
		this.broadcastChanges();
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex < 0 || slotIndex >= this.slots.size()) return ItemStack.EMPTY;

		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) return ItemStack.EMPTY;

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();
		boolean moved;
		if (slotIndex < PLAYER_INVENTORY_START) {
			moved = this.moveItemStackTo(stack, PLAYER_INVENTORY_START, this.slots.size(), false);
		} else if (isValidItem(stack)) {
			moved = this.moveItemStackTo(stack, UPGRADE_SLOT_START, UPGRADE_SLOT_START + 1, false);
		} else if (slotIndex < HOTBAR_START) {
			moved = this.moveItemStackTo(stack, HOTBAR_START, this.slots.size(), false);
		} else {
			moved = this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_START, false);
		}

		if (!moved) return ItemStack.EMPTY;
		if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
		else slot.setChanged();
		if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;

		slot.onTake(player, stack);
		return original;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!(player instanceof ServerPlayer)) return;

		for (int index = 0; index < upgradeInventory.getContainerSize(); index++) {
			ItemStack stack = upgradeInventory.getItem(index);
			if (stack.isEmpty()) continue;
			player.getInventory().placeItemBackInInventory(stack.copy(), Prediction.SERVER_ONLY);
			upgradeInventory.setItem(index, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	private int countItemCopies(ItemStack target) {
		int count = 0;
		int startingLevel = ItemsCategoriesAPIManager.getItemStartingLevel();
		for (int index = PLAYER_INVENTORY_START; index < this.slots.size(); index++) {
			ItemStack stack = this.slots.get(index).getItem();
			if (stack.isEmpty() || stack.getItem() != target.getItem() || !isValidItem(stack) || !sameRarity(stack, target)) continue;
			if (itemLevel(stack) == startingLevel) count += stack.getCount();
		}
		return count;
	}

	private Slot displayOnlySlot(int inventoryIndex, int x, int y) {
		return new Slot(upgradeInventory, inventoryIndex, x, y) {
			@Override public boolean mayPlace(ItemStack stack) { return false; }
			@Override public boolean mayPickup(Player player) { return false; }
		};
	}

	private int countItems(Item item) {
		int count = 0;
		for (int index = PLAYER_INVENTORY_START; index < this.slots.size(); index++) {
			ItemStack stack = this.slots.get(index).getItem();
			if (stack.is(item)) count += stack.getCount();
		}
		return count;
	}

	private void consumeItemCopies(ItemStack target, int amount) {
		int startingLevel = ItemsCategoriesAPIManager.getItemStartingLevel();
		consumeItemsMatching(amount, stack -> stack.getItem() == target.getItem()
			&& isValidItem(stack)
			&& sameRarity(stack, target)
			&& itemLevel(stack) == startingLevel);
	}

	private void consumeItems(Item item, int amount) {
		consumeItemsMatching(amount, stack -> stack.is(item));
	}

	private void consumeItemsMatching(int amount, java.util.function.Predicate<ItemStack> predicate) {
		int remaining = amount;
		for (int index = PLAYER_INVENTORY_START; index < this.slots.size() && remaining > 0; index++) {
			Slot slot = this.slots.get(index);
			ItemStack stack = slot.getItem();
			if (stack.isEmpty() || !predicate.test(stack)) continue;

			int consumed = Math.min(remaining, stack.getCount());
			stack.shrink(consumed);
			remaining -= consumed;
			if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
			else slot.setChanged();
		}
	}

	private static boolean isValidItem(ItemStack stack) {
		return ItemsCategoriesAPIManager.areItemLevelsEnabled()
			&& ItemsCategoriesAPIManager.isRarityCategoryItem(stack);
	}

	private static int itemLevel(ItemStack stack) {
		Integer level = ItemsCategoriesAPIManager.getItemLevel(stack);
		return level == null ? ItemsCategoriesAPIManager.getItemStartingLevel() : level;
	}

	private static boolean sameRarity(ItemStack first, ItemStack second) {
		return RarityAPIManager.detectAppliedRarity(first) == RarityAPIManager.detectAppliedRarity(second);
	}

	private static int experienceBottleCost(ItemStack target, int level) {
		int startingLevel = ItemsCategoriesAPIManager.getItemStartingLevel();
		int levelStep = Math.max(1, level - startingLevel + 1);
		return rarityExperienceStep(target) * levelStep + materialIndex(target) * EXPERIENCE_MATERIAL_STEP;
	}

	private static int rarityExperienceStep(ItemStack target) {
		RarityAPIManager.Tier rarity = RarityAPIManager.detectAppliedRarity(target);
		if (rarity == null) rarity = RarityAPIManager.Tier.COMMON;
		return switch (rarity) {
			case COMMON -> 2;
			case RARE -> 4;
			case EPIC -> 6;
			case LEGENDARY -> 8;
			case MYTHIC -> 10;
		};
	}

	private static int materialIndex(ItemStack target) {
		if (target == null || target.isEmpty()) return 0;
		String path = BuiltInRegistries.ITEM.getKey(target.getItem()).getPath();
		if (path.contains("netherite")) return 6;
		if (path.contains("diamond")) return 5;
		if (path.contains("golden") || path.contains("gold")) return 4;
		if (path.contains("iron")) return 3;
		if (path.contains("copper")) return 2;
		if (path.contains("stone")) return 1;
		return 0;
	}

	public record UpgradeRequirement(int owned, int required) {
		public boolean isMet() { return owned >= required; }
		public String displayText() { return owned + "/" + required; }
	}

	public record UpgradeRequirements(
		boolean hasTarget,
		boolean canUpgrade,
		UpgradeRequirement itemCopies,
		UpgradeRequirement experienceBottles,
		UpgradeRequirement essence
	) {
		private static UpgradeRequirements empty() {
			UpgradeRequirement emptyRequirement = new UpgradeRequirement(0, 0);
			return new UpgradeRequirements(false, false, emptyRequirement, emptyRequirement, emptyRequirement);
		}
	}
}
