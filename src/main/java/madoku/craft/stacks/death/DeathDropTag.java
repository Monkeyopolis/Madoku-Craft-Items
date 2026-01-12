package madoku.craft.stacks.death;

import madoku.craft.stacks.config.StackingConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class DeathDropTag {
	private static final String TAG_KEY = "madokuCraftStacksDeathDrop";

	private DeathDropTag() {
	}

	public static void mark(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putBoolean(TAG_KEY, true));
	}

	public static boolean isMarked(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}

		NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (custom == null || custom.isEmpty()) {
			return false;
		}

		NbtCompound nbt = custom.copyNbt();
		return nbt.getBoolean(TAG_KEY, false);
	}

	public static int getDeathDropDespawnTicks() {
		int minutes = StackingConfig.getDeathDropDespawnMinutes();
		long ticks = (long) Math.max(1, minutes) * 60L * 20L;
		if (ticks > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) ticks;
	}
}
