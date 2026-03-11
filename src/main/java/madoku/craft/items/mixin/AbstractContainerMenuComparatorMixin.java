package madoku.craft.items.mixin;

import madoku.craft.items.itemstack.system.MadokuItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuComparatorMixin {
	@Inject(
		method = "getRedstoneSignalFromContainer(Lnet/minecraft/world/Container;)I",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void madokuCraft$useVanillaComparatorScaling(
		Container container,
		CallbackInfoReturnable<Integer> cir
	) {
		if (container == null || !MadokuItemStack.useVanillaComparatorScaling()) {
			return;
		}

		int slots = container.getContainerSize();
		if (slots <= 0) {
			cir.setReturnValue(0);
			return;
		}

		int filledSlots = 0;
		float fullness = 0.0F;

		for (int index = 0; index < slots; index++) {
			ItemStack stack = container.getItem(index);
			if (stack.isEmpty()) {
				continue;
			}

			int vanillaMax = Math.min(64, stack.getItem().getDefaultMaxStackSize());
			if (vanillaMax <= 0) {
				continue;
			}

			int countedItems = Math.min(stack.getCount(), vanillaMax);
			fullness += (float) countedItems / (float) vanillaMax;
			filledSlots++;
		}

		fullness /= (float) slots;
		int signal = Mth.floor(fullness * 14.0F);
		if (filledSlots > 0) {
			signal += 1;
		}
		cir.setReturnValue(signal);
	}
}
