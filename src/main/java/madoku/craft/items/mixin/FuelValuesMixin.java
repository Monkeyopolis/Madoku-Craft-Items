package madoku.craft.items.mixin;

import madoku.craft.items.item.system.MadokuItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FuelValuesMixin {
	@Inject(method = "isFuel", at = @At("HEAD"), cancellable = true)
	private static void madokuCraft$restrictFuelToConfiguredFuelItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!MadokuItem.isEnabled()) {
			return;
		}

		cir.setReturnValue(MadokuItem.isConfiguredFuel(stack));
	}
}
