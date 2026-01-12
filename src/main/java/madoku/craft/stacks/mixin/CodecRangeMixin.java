package madoku.craft.stacks.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import madoku.craft.stacks.config.StackingConfig;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Codecs.class)
public abstract class CodecRangeMixin {
	@Overwrite
	public static Codec<Integer> rangedInt(int min, int max) {
		int upper = StackingConfig.isEnabled()
			? Math.max(max, StackingConfig.MAX_STACK_CAP)
			: max;

		return Codec.INT.flatXmap(
			value -> {
				if (value < min) {
					return DataResult.error(() -> "Value must be within range [" + min + ";" + upper + "]: " + value);
				}

				if (value > upper) {
					return DataResult.success(upper);
				}

				return DataResult.success(value);
			},
			value -> value.compareTo(min) >= 0 && value.compareTo(upper) <= 0
				? DataResult.success(value)
				: DataResult.error(() -> "Value must be within range [" + min + ";" + upper + "]: " + value)
		);
	}
}
