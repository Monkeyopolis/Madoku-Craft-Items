package madoku.craft.stacks.mixin;

import madoku.craft.stacks.config.StackingConfig;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface InventoryStackLimitMixin {
	@Inject(method = "getMaxCountPerStack", at = @At("RETURN"), cancellable = true)
	private void madokuCraftStacks$raiseMaxCountPerStack(CallbackInfoReturnable<Integer> cir) {
		if (!StackingConfig.isEnabled()) {
			return;
		}

		int limit = StackingConfig.getStackLimit();
		int original = cir.getReturnValue();
		if (limit <= 1 || original <= 1) {
			return;
		}

		int adjusted = Math.max(original, limit);
		if (adjusted != original) {
			cir.setReturnValue(adjusted);
		}
	}

	@Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
	private void madokuCraftStacks$allowOverLimit(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (!StackingConfig.isEnabled()) {
			return;
		}

		int current = stack.getCount();
		int allowed = cir.getReturnValue();
		if (current > allowed) {
			cir.setReturnValue(current);
		}
	}
}
