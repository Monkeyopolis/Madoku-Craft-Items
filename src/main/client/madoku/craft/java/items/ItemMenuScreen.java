package madoku.craft.java.items;

import madoku.craft.java.core.menu.MenuScreen;
import madoku.craft.java.items.ItemPayloadAPIManager.UpgradeItemPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Dedicated screen for item-level upgrade inputs. */
public final class ItemMenuScreen extends AbstractContainerScreen<ItemMenu> {
	private static final Identifier CONTAINER_TEXTURE = texture("madoku-menu/items-menu-container.png");
	private static final Identifier[] ITEM_SLOT_TEXTURES = {
		texture("shared-ui/chestplate-slot.png"),
		texture("shared-ui/pickaxe-slot.png"),
		texture("shared-ui/sword-slot.png")
	};
	private static final Identifier BOTTLE_SLOT_TEXTURE = texture("shared-ui/bottle-slot.png");
	private static final Identifier ESSENCE_SLOT_TEXTURE = texture("shared-ui/essence-slot.png");
	private static final Identifier UPGRADE_TEXTURE = texture("shared-ui/upgrade-button.png");
	private static final Identifier UPGRADE_HIGHLIGHTED_TEXTURE = texture("shared-ui/upgrade-button-highlighted.png");
	private static final Identifier EXIT_TEXTURE = texture("shared-ui/exit-button.png");
	private static final Identifier EXIT_HIGHLIGHTED_TEXTURE = texture("shared-ui/exit-button-highlighted.png");
	private static final int PANEL_WIDTH = 176;
	private static final int PANEL_HEIGHT = 224;
	private static final int UPGRADE_BUTTON_X = 61;
	private static final int UPGRADE_BUTTON_Y = 103;
	private static final int UPGRADE_BUTTON_WIDTH = 54;
	private static final int UPGRADE_BUTTON_HEIGHT = 14;
	private static final int EXIT_X = 157;
	private static final int EXIT_Y = 7;
	private static final int EXIT_SIZE = 12;
	private static final int HEADER_CENTER_X = 88;
	private static final int HEADER_TEXT_COLOR = 0xFF404040;
	private static final float HEADER_TEXT_SCALE = 0.8F;
	private int placeholderTick;

	public ItemMenuScreen(ItemMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, PANEL_WIDTH, PANEL_HEIGHT);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void containerTick() {
		super.containerTick();
		placeholderTick++;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, leftPos, topPos, 0.0F, 0.0F, 256, 256, 256, 256);

		if (!getMenu().slots.get(ItemMenu.UPGRADE_SLOT_START).hasItem()) {
			blitSlotIcon(graphics, itemPlaceholderTexture(), ItemMenu.TARGET_SLOT_X, ItemMenu.TARGET_SLOT_Y);
		}

		ItemMenu.UpgradeRequirements requirements = getMenu().getUpgradeRequirements();
		drawUpgradeRequirements(graphics, requirements);

		boolean upgradeHovered = requirements.canUpgrade()
			&& contains(leftPos + UPGRADE_BUTTON_X, topPos + UPGRADE_BUTTON_Y, UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_HEIGHT, mouseX, mouseY);
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			upgradeHovered ? UPGRADE_HIGHLIGHTED_TEXTURE : UPGRADE_TEXTURE,
			leftPos + UPGRADE_BUTTON_X,
			topPos + UPGRADE_BUTTON_Y,
			0.0F,
			0.0F,
			UPGRADE_BUTTON_WIDTH,
			UPGRADE_BUTTON_HEIGHT,
			UPGRADE_BUTTON_WIDTH,
			UPGRADE_BUTTON_HEIGHT
		);
		drawTextCenteredInButton(graphics, Component.translatable("menu.madoku-craft.items.upgrade").getString());

		boolean exitHovered = contains(leftPos + EXIT_X, topPos + EXIT_Y, EXIT_SIZE, EXIT_SIZE, mouseX, mouseY);
		graphics.blit(
			RenderPipelines.GUI_TEXTURED,
			exitHovered ? EXIT_HIGHLIGHTED_TEXTURE : EXIT_TEXTURE,
			leftPos + EXIT_X,
			topPos + EXIT_Y,
			0.0F,
			0.0F,
			EXIT_SIZE,
			EXIT_SIZE,
			EXIT_SIZE,
			EXIT_SIZE
		);
		drawCenteredText(graphics, Component.translatable("menu.madoku-craft.items.title").getString(), HEADER_CENTER_X, 7);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		// The background art supplies the inventory layout; only the custom heading is shown.
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 1) return super.mouseClicked(event, doubleClick);
		int mouseX = (int) event.x();
		int mouseY = (int) event.y();
		if (contains(leftPos + EXIT_X, topPos + EXIT_Y, EXIT_SIZE, EXIT_SIZE, mouseX, mouseY)) {
			super.onClose();
			Minecraft.getInstance().setScreenAndShow(new MenuScreen());
			return true;
		}
		if (contains(leftPos + UPGRADE_BUTTON_X, topPos + UPGRADE_BUTTON_Y, UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_HEIGHT, mouseX, mouseY)) {
			if (getMenu().getUpgradeRequirements().canUpgrade()) {
				ClientPlayNetworking.send(new UpgradeItemPayload());
			}
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	private void drawUpgradeRequirements(GuiGraphicsExtractor graphics, ItemMenu.UpgradeRequirements requirements) {
		int itemX = ItemMenu.ITEM_REQUIREMENT_SLOT_X;
		int bottleX = ItemMenu.EXPERIENCE_REQUIREMENT_SLOT_X;
		int essenceX = ItemMenu.ESSENCE_REQUIREMENT_SLOT_X;
		int slotY = ItemMenu.REQUIREMENT_SLOT_Y;
		if (!requirements.hasTarget()) {
			blitSlotIcon(graphics, itemPlaceholderTexture(), itemX, slotY);
			blitSlotIcon(graphics, BOTTLE_SLOT_TEXTURE, bottleX - 1, slotY - 1);
			blitSlotIcon(graphics, ESSENCE_SLOT_TEXTURE, essenceX, slotY);
			return;
		}

		ItemStack targetItem = getMenu().slots.get(ItemMenu.UPGRADE_SLOT_START).getItem().copy();
		ItemsCategoriesAPIManager.setItemLevel(targetItem, ItemsCategoriesAPIManager.getItemStartingLevel());
		drawRequirement(graphics, targetItem, requirements.itemCopies(), itemX, slotY);
		drawRequirement(graphics, new ItemStack(Items.EXPERIENCE_BOTTLE), requirements.experienceBottles(), bottleX, slotY);
		drawRequirement(graphics, new ItemStack(EssenceManager.ESSENCE), requirements.essence(), essenceX, slotY);
	}

	private void drawRequirement(
		GuiGraphicsExtractor graphics,
		ItemStack requiredItem,
		ItemMenu.UpgradeRequirement requirement,
		int slotX,
		int slotY
	) {
		graphics.item(requiredItem, leftPos + slotX, topPos + slotY);
		drawRequirementText(graphics, requirement, slotX, slotY);
	}

	private void drawRequirementText(
		GuiGraphicsExtractor graphics,
		ItemMenu.UpgradeRequirement requirement,
		int slotX,
		int slotY
	) {
		String text = requirement.displayText();
		float scale = Math.min(0.7F, 14.0F / Math.max(1, this.font.width(text)));
		float textWidth = this.font.width(text) * scale;
		float textHeight = this.font.lineHeight * scale;
		int x = Math.round(leftPos + slotX + 8.0F - textWidth / 2.0F);
		int y = Math.round(topPos + slotY + 16.0F - textHeight);
		int color = requirement.isMet() ? 0xFF55FF55 : 0xFFFF5555;
		graphics.pose().pushMatrix();
		graphics.pose().scale(scale, scale);
		graphics.text(this.font, text, Math.round(x / scale), Math.round(y / scale), color, true);
		graphics.pose().popMatrix();
	}

	private void drawTextCenteredInButton(GuiGraphicsExtractor graphics, String text) {
		float scale = HEADER_TEXT_SCALE;
		float textWidth = this.font.width(text) * scale;
		float textHeight = this.font.lineHeight * scale;
		int x = Math.round(leftPos + UPGRADE_BUTTON_X + UPGRADE_BUTTON_WIDTH / 2.0F - textWidth / 2.0F);
		int y = Math.round(topPos + UPGRADE_BUTTON_Y + UPGRADE_BUTTON_HEIGHT / 2.0F - textHeight / 2.0F) + 1;
		graphics.pose().pushMatrix();
		graphics.pose().scale(scale, scale);
		graphics.text(this.font, text, Math.round(x / scale), Math.round(y / scale), HEADER_TEXT_COLOR, false);
		graphics.pose().popMatrix();
	}

	private void drawCenteredText(GuiGraphicsExtractor graphics, String text, int centerX, int y) {
		int textWidth = Math.round(this.font.width(text) * HEADER_TEXT_SCALE);
		int x = leftPos + centerX - textWidth / 2;
		graphics.pose().pushMatrix();
		graphics.pose().scale(HEADER_TEXT_SCALE, HEADER_TEXT_SCALE);
		graphics.text(this.font, text, Math.round(x / HEADER_TEXT_SCALE), Math.round((topPos + y) / HEADER_TEXT_SCALE), HEADER_TEXT_COLOR, false);
		graphics.pose().popMatrix();
	}

	private void blitSlotIcon(GuiGraphicsExtractor graphics, Identifier texture, int x, int y) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + x, topPos + y, 0.0F, 0.0F, 16, 16, 16, 16);
	}

	private Identifier itemPlaceholderTexture() {
		return ITEM_SLOT_TEXTURES[(placeholderTick / 40) % ITEM_SLOT_TEXTURES.length];
	}

	private static boolean contains(int x, int y, int width, int height, int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private static Identifier texture(String path) {
		return Identifier.fromNamespaceAndPath("madoku-craft", "textures/" + path);
	}
}
