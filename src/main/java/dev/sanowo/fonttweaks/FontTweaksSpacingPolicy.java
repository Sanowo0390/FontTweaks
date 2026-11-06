package dev.sanowo.fonttweaks;

import net.minecraft.util.FormattedCharSequence;

/**
 * A spacing exemption is per glyph for horizontal tracking and per rendered
 * line for vertical line spacing. A mixed Japanese/English line is considered
 * non-English, so its line height remains uniform.
 */
public final class FontTweaksSpacingPolicy {
    private FontTweaksSpacingPolicy() {}

    public static boolean containsNonEnglish(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.codePoints().anyMatch(cp -> Character.isLetterOrDigit(cp)
                && !FontTweaksSettings.isEnglish(cp));
    }

    public static boolean containsNonEnglish(FormattedCharSequence line) {
        if (line == null) return false;
        final boolean[] found = {false};
        line.accept((offset, style, cp) -> {
            if (Character.isLetterOrDigit(cp) && !FontTweaksSettings.isEnglish(cp)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }

    public static boolean applyLineSpacing(FormattedCharSequence line, boolean excludeEnglish) {
        return !excludeEnglish || containsNonEnglish(line);
    }

    public static int lineStep(int vanilla, double factor, FormattedCharSequence line,
                               boolean excludeEnglish) {
        if (!applyLineSpacing(line, excludeEnglish)) return vanilla;
        return Math.max(1, (int) Math.round(vanilla * factor));
    }
}

