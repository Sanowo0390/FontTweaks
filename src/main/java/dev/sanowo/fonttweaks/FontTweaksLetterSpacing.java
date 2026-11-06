package dev.sanowo.fonttweaks;

import net.minecraft.client.Minecraft;

/**
 * Horizontal tracking shared by both glyph measurement and the draw pen.
 * The codepoint parameter ensures the English-exclusion toggle behaves the
 * same in wrapping, centering, hovering, and glyph emission.
 */
public final class FontTweaksLetterSpacing {
    private FontTweaksLetterSpacing() {}

    public static float adjustedAdvance(float original, double letterSpacing) {
        if (!Double.isFinite(letterSpacing) || letterSpacing == 0.0) return original;
        return Math.max(0.5f, (float) (original + letterSpacing));
    }

    public static float adjustedAdvance(float original, double spacing,
                                        int codepoint, boolean excludeEnglish) {
        if (excludeEnglish && codepoint >= 0 && FontTweaksSettings.isEnglish(codepoint)) {
            return original;
        }
        return adjustedAdvance(original, spacing);
    }

    /** Used by both the StringSplitter and the actual PreparedTextBuilder. */
    public static float apply(float original, int codepoint) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        if (cfg.letterSpacing == 0.0) return original;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gui != null
                && FontTweaksUiContext.isOwnYaclScreen(mc.gui.screen())
                && FontTweaksRenderContext.current() != FontTweaksRenderContext.Kind.PREVIEW) {
            return original;
        }
        return adjustedAdvance(original, cfg.letterSpacing,
                codepoint, cfg.excludeEnglishFromSpacing);
    }

}

