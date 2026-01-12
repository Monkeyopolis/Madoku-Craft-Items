package madoku.craft.stacks.mixin;

import madoku.craft.stacks.death.DeathDropHandler;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryDropMixin {
	@Inject(method = "dropAll", at = @At("HEAD"), cancellable = true)
	private void madokuCraftStacks$dropSome(CallbackInfo ci) {
		if (DeathDropHandler.handleDrop((PlayerInventory) (Object) this)) {
			ci.cancel();
		}
	}
}
