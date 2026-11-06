package dev.sanowo.fonttweaks;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;

/**
 * Horizontal tracking (letter spacing) in GUI units.
 *
 * Both the StringSplitter's per-codepoint width provider and the glyph
 * renderer must call this function. Otherwise wrapping, chat clicks,
 * centering and actual glyph positions disagree.
 */
public final class FontTweaksLetterSpacing {
    private FontTweaksLetterSpacing() {}

    public static float adjustedAdvance(float original, double letterSpacing) {
        if (!Double.isFinite(letterSpacing) || letterSpacing == 0.0) return original;
        // Prevent negative/zero glyph advances on narrow fonts.
        return Math.max(0.5f, (float) (original + letterSpacing));
    }

    public static float apply(float original) {
        double letterSpacing = FontTweaksSettings.get().letterSpacing;
        if (letterSpacing == 0.0) return original;
        Minecraft mc = Minecraft.getInstance();
        // Do not distort the configuration UI's own hitboxes and labels.
        if (mc != null && mc.gui != null && mc.gui.screen() instanceof YACLScreen
                && FontTweaksRenderContext.current() != FontTweaksRenderContext.Kind.PREVIEW) {
            return original;
        }
        return adjustedAdvance(original, letterSpacing);
    }
}
