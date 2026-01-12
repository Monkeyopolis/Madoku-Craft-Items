package madoku.craft.stacks.mixin;

import madoku.craft.stacks.death.DeathDropTag;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityDespawnMixin {
	private static final int VANILLA_DESPAWN_TICKS = 6000;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void madokuCraftStacks$earlyDespawn(CallbackInfo ci) {
		ItemEntity entity = (ItemEntity) (Object) this;
		if (entity.getEntityWorld().isClient()) {
			return;
		}

		ItemStack stack = entity.getStack();
		if (!DeathDropTag.isMarked(stack)) {
			return;
		}

		int age = entity.getItemAge();
		int limit = DeathDropTag.getDeathDropDespawnTicks();
		if (age >= limit) {
			entity.discard();
			ci.cancel();
		}
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;discard()V"))
	private void madokuCraftStacks$extendDeathDropDespawn(ItemEntity entity) {
		ItemStack stack = entity.getStack();
		if (DeathDropTag.isMarked(stack)) {
			int age = entity.getItemAge();
			int limit = DeathDropTag.getDeathDropDespawnTicks();
			if (limit > VANILLA_DESPAWN_TICKS && age >= VANILLA_DESPAWN_TICKS && age < limit) {
				return;
			}
		}

		entity.discard();
	}
}
