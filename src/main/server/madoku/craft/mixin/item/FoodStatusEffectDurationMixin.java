package madoku.craft.mixin.item;

import java.util.List;

import madoku.craft.java.items.ItemsCategoriesManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyStatusEffectsConsumeEffect.class)
public abstract class FoodStatusEffectDurationMixin {
	@Shadow
	public abstract List<MobEffectInstance> effects();

	@Shadow
	public abstract float probability();

	@Inject(method = "apply", at = @At("HEAD"), cancellable = true)
	private void madokuCraft$addFoodEffectDuration(Level level, ItemStack stack, LivingEntity entity,
		CallbackInfoReturnable<Boolean> cir) {
		if (!ItemsCategoriesManager.isFoodCategoryItem(stack)) {
			return;
		}

		if (entity.getRandom().nextFloat() >= probability()) {
			cir.setReturnValue(false);
			return;
		}

		boolean applied = false;
		for (MobEffectInstance configuredEffect : effects()) {
			if (configuredEffect == null) {
				continue;
			}

			MobEffectInstance existingEffect = entity.getEffect(configuredEffect.getEffect());
			MobEffectInstance effectToApply = configuredEffect;
			int existingDuration = existingEffect == null ? 0 : Math.max(0, existingEffect.getDuration());
			int addedDuration = Math.max(0, configuredEffect.getDuration());
			if (existingEffect != null && !existingEffect.isInfiniteDuration()
				&& !configuredEffect.isInfiniteDuration()) {
				int combinedDuration = (int) Math.min(
					Integer.MAX_VALUE,
					(long) existingDuration + addedDuration
				);
				effectToApply = new MobEffectInstance(
					configuredEffect.getEffect(),
					combinedDuration,
					existingEffect.getAmplifier(),
					existingEffect.isAmbient(),
					existingEffect.isVisible(),
					existingEffect.showIcon()
				);
			}

			if (entity.addEffect(new MobEffectInstance(effectToApply))) {
				applied = true;
			}
		}

		cir.setReturnValue(applied);
	}
}
