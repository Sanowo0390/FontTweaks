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
        if (mc == null || mc.gui == null) return;

        FontTweaksRenderContext.Kind context = FontTweaksRenderContext.current();
        // Keep the configuration controls readable. The explicitly tagged
        // preview is the only text in this screen that uses user settings.
        if (mc.gui.screen() instanceof FontTweaksScreen
                && context != FontTweaksRenderContext.Kind.PREVIEW) return;

        FontTweaksSettings settings = FontTweaksSettings.get();
        double multiplier = switch (context) {
            case BUTTON -> settings.globalScale * settings.screenScale * settings.buttonScale;
            // Vanilla chat already scales its own pose, including backgrounds.
            case CHAT -> 1.0;
            case PREVIEW -> settings.scaleFor(true);
            case DEFAULT -> settings.scaleFor(mc.gui.screen() != null);
        };

        if (Math.abs(multiplier - 1.0) < 0.000001) return;

        float scale = (float) FontTweaksSettings.bounded(multiplier, 0.05, 8.0);
        // Button text is centered in the widget: preserve its center.
        float anchorX = (context == FontTweaksRenderContext.Kind.BUTTON
                || context == FontTweaksRenderContext.Kind.PREVIEW)
                ? x + font.width(text) / 2.0f : x;

        Matrix3x2f adjusted = new Matrix3x2f(inputPose);
        adjusted.translate(anchorX, y).scale(scale, scale).translate(-anchorX, -y);

        this.pose = adjusted;
    }

}
