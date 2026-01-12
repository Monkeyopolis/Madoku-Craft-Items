package madoku.craft.stacks.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
	private static final float STACK_COUNT_SCALE = 0.9f;

	@Shadow
	private Matrix3x2fStack matrices;

	@Shadow
	protected abstract void drawText(TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow);

	@Redirect(
		method = "drawStackCount(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)V"
		)
	)
	private void madokuCraftStacks$scaleStackCount(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow) {
		if (!shouldScale(text)) {
			drawText(textRenderer, text, x, y, color, shadow);
			return;
		}

		int width = textRenderer.getWidth(text);
		this.matrices.pushMatrix();
		this.matrices.translate(x + width, y);
		this.matrices.scale(STACK_COUNT_SCALE, STACK_COUNT_SCALE);
		this.matrices.translate(-x - width, -y);
		this.drawText(textRenderer, text, x, y, color, shadow);
		this.matrices.popMatrix();
	}

	private static boolean shouldScale(String text) {
		if (text == null || text.isEmpty()) {
			return false;
		}

		try {
			return Integer.parseInt(text) > 99;
		} catch (NumberFormatException ignored) {
			return text.length() > 2;
		}
	}
}
