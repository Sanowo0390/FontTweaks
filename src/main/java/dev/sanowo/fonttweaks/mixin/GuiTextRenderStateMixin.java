package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksRenderContext;
import dev.sanowo.fonttweaks.FontTweaksScreen;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jspecify.annotations.Nullable;

/**
 * MC 26.3's GuiTextRenderState is shared by both legacy graphics.text and
 * ActiveTextCollector. Hooking here covers regular text, centered labels,
 * buttons and text drawn by many screens.
 */
@Mixin(GuiTextRenderState.class)
public abstract class GuiTextRenderStateMixin {
    @Shadow @Final @Mutable public Matrix3x2fc pose;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fonttweaks$adjustTextPose(Font font, FormattedCharSequence text,
            Matrix3x2fc inputPose, int x, int y, int color, int backgroundColor,
            boolean dropShadow, boolean includeEmpty, @Nullable ScreenRectangle scissor,
            CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null || mc.gui.screen() instanceof FontTweaksScreen) return;

        FontTweaksSettings settings = FontTweaksSettings.get();
        FontTweaksRenderContext.Kind context = FontTweaksRenderContext.current();
        double multiplier = switch (context) {
            case BUTTON -> settings.globalScale * settings.screenScale * settings.buttonScale;
            // Vanilla chat already scales its own pose, including backgrounds.
            case CHAT -> 1.0;
            case DEFAULT -> settings.scaleFor(mc.gui.screen() != null);
        };

        int guiScale = mc.getWindow().getGuiScale();
        boolean pixelFix = settings.unifontPixelPerfect && guiScale == 3
                && fonttweaks$hasEastAsianGlyphs(text);
        if (pixelFix) {
            // Unihex/Unifont glyphs are 16 source pixels tall but displayed at
            // about 8 GUI units. At GUI scale 3, vanilla uses 1.5 physical
            // pixels per source dot. Choose exactly 1 or 2 pixels per dot.
            double existingScale = Math.hypot(inputPose.m00(), inputPose.m01());
            if (existingScale > 0.001) {
                multiplier = settings.unifontPixelsPerDot * 2.0 / (guiScale * existingScale);
            }
        }
        if (Math.abs(multiplier - 1.0) < 0.000001 && !pixelFix) return;

        float scale = (float) FontTweaksSettings.bounded(multiplier, 0.05, 8.0);
        // Button text is centered in the widget: preserve its center.
        float anchorX = context == FontTweaksRenderContext.Kind.BUTTON
                ? x + font.width(text) / 2.0f : x;

        Matrix3x2f adjusted = new Matrix3x2f(inputPose);
        adjusted.translate(anchorX, y).scale(scale, scale).translate(-anchorX, -y);

        if (pixelFix) {
            // Snap screen-space origin to the physical pixel grid to prevent
            // subpixel shimmer when the rest of the GUI contains translations.
            float guiX = adjusted.m00() * anchorX + adjusted.m10() * y + adjusted.m20();
            float guiY = adjusted.m01() * anchorX + adjusted.m11() * y + adjusted.m21();
            adjusted.translateLocal(
                    Math.round(guiX * guiScale) / (float) guiScale - guiX,
                    Math.round(guiY * guiScale) / (float) guiScale - guiY);
        }
        this.pose = adjusted;
    }

    private static boolean fonttweaks$hasEastAsianGlyphs(FormattedCharSequence sequence) {
        final boolean[] found = {false};
        sequence.accept((index, style, cp) -> {
            if ((cp >= 0x3000 && cp <= 0x30FF)
                    || (cp >= 0x3400 && cp <= 0x9FFF)
                    || (cp >= 0xAC00 && cp <= 0xD7AF)
                    || (cp >= 0xF900 && cp <= 0xFAFF)
                    || (cp >= 0xFF00 && cp <= 0xFFEF)
                    || (cp >= 0x20000 && cp <= 0x3134F)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }
}
