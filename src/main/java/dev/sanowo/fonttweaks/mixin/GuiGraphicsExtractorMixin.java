package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.sanowo.fonttweaks.FontTweaksScreen;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Scale only emitted glyphs around their original origin, never the entire GUI.
 * The guard prevents double scaling from text overloads delegating to each other.
 *
 * Layout geometry and hitboxes remain vanilla-sized: very large values can overlap.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @Unique private static final ThreadLocal<Boolean> fonttweaks$inside = ThreadLocal.withInitial(() -> false);

    @Unique
    private float fonttweaks$scale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) return 1.0f;
        var screen = minecraft.gui.screen();
        if (screen instanceof FontTweaksScreen) return 1.0f;
        return FontTweaksSettings.get().scaleFor(screen != null);
    }

    @Unique
    private void fonttweaks$drawScaled(int x, int y, Runnable original) {
        float scale = fonttweaks$scale();
        if (fonttweaks$inside.get() || Math.abs(scale - 1.0f) < 0.001f) {
            original.run();
            return;
        }
        Matrix3x2fStack pose = ((GuiGraphicsExtractor) (Object) this).pose();
        fonttweaks$inside.set(true);
        pose.pushMatrix();
        try {
            // Keep the top-left text anchor unchanged when scaling.
            pose.translate((1.0f - scale) * x, (1.0f - scale) * y);
            pose.scale(scale, scale);
            original.run();
        } finally {
            pose.popMatrix();
            fonttweaks$inside.set(false);
        }
    }

    @WrapMethod(method = "text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V")
    private void fonttweaks$textString(Font font, String str, int x, int y, int color,
                                       boolean shadow, Operation<Void> original) {
        fonttweaks$drawScaled(x, y, () -> original.call(font, str, x, y, color, shadow));
    }

    @WrapMethod(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V")
    private void fonttweaks$textSequence(Font font, FormattedCharSequence str, int x, int y,
                                         int color, boolean shadow, Operation<Void> original) {
        fonttweaks$drawScaled(x, y, () -> original.call(font, str, x, y, color, shadow));
    }

    @WrapMethod(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V")
    private void fonttweaks$textComponent(Font font, Component str, int x, int y, int color,
                                          boolean shadow, Operation<Void> original) {
        fonttweaks$drawScaled(x, y, () -> original.call(font, str, x, y, color, shadow));
    }

    /** Multiline word wrapping, not chat. Vanilla chat already exposes line spacing options. */
    @WrapMethod(method = "textWithWordWrap(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/FormattedText;IIIIZ)I")
    private int fonttweaks$textWithWordWrap(Font font, FormattedText text, int x, int y,
                                           int width, int color, boolean shadow, Operation<Integer> original) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        double spacing = cfg.wrappedLineSpacing;
        float scale = fonttweaks$scale();
        if (Math.abs(spacing - 1.0f) < 0.001f && Math.abs(scale - 1.0f) < 0.001f) {
            return original.call(font, text, x, y, width, color, shadow);
        }
        int wrapWidth = Math.max(1, Math.round(width / scale));
        int lineStep = Math.max(1, (int) Math.round(font.lineHeight * spacing * scale));
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        int nextY = y;
        for (FormattedCharSequence line : font.split(text, wrapWidth)) {
            graphics.text(font, line, x, nextY, color, shadow);
            nextY += lineStep;
        }
        return nextY;
    }
}
