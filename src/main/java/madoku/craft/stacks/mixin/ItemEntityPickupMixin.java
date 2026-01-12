package madoku.craft.stacks.mixin;

import madoku.craft.stacks.death.DeathDropTag;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityPickupMixin {
	@Unique
	private boolean madokuCraftStacks$wasDeathDrop;

	@Inject(method = "onPlayerCollision", at = @At("HEAD"))
	private void madokuCraftStacks$trackDeathDrop(PlayerEntity player, CallbackInfo ci) {
		ItemEntity entity = (ItemEntity) (Object) this;
		madokuCraftStacks$wasDeathDrop = !entity.getEntityWorld().isClient()
			&& DeathDropTag.isMarked(entity.getStack());
		if (madokuCraftStacks$wasDeathDrop) {
			DeathDropTag.clear(entity.getStack());
		}
	}

	@Inject(method = "onPlayerCollision", at = @At("TAIL"))
	private void madokuCraftStacks$clearDeathDropTag(PlayerEntity player, CallbackInfo ci) {
		if (!madokuCraftStacks$wasDeathDrop || player == null) {
			return;
		}

		ItemEntity entity = (ItemEntity) (Object) this;
		if (!entity.getStack().isEmpty()) {
			DeathDropTag.mark(entity.getStack());
		}

		for (int i = 0; i < player.getInventory().size(); i++) {
			DeathDropTag.clear(player.getInventory().getStack(i));
		}
		madokuCraftStacks$wasDeathDrop = false;
	}
}
