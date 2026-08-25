package madoku.craft.items.mixin;

import madoku.craft.items.ItemsCategoriesManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FuelValues.class)
public abstract class FuelValuesMixin {
	@Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$restrictFuelToConfiguredFuelItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!ItemsCategoriesManager.isEnabled()) {
			return;
		}

		boolean configuredFuel = ItemsCategoriesManager.isConfiguredFuel(stack);
		if (configuredFuel) {
			cir.setReturnValue(true);
		}
	}
}
