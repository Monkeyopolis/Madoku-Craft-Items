package madoku.craft.stacks.mixin;

import madoku.craft.stacks.config.StackingConfig;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "getMaxCount", at = @At("RETURN"), cancellable = true)
	private void madokuCraftStacks$adjustMaxCount(CallbackInfoReturnable<Integer> cir) {
		if (!StackingConfig.isEnabled()) {
			return;
		}

		int original = cir.getReturnValue();
		int configured = StackingConfig.getStackLimit();
		if (configured <= 1 || original <= 1) {
			return;
		}

		int adjusted = Math.max(original, configured);
		if (adjusted != original) {
			cir.setReturnValue(adjusted);
		}
	}
}
