package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LetterSpacingTest {
    @Test
    void horizontalTrackingChangesGlyphAdvanceNotVerticalLineDistance() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        assertEquals(0.0, cfg.letterSpacing);
        assertEquals(6.0f, FontTweaksLetterSpacing.adjustedAdvance(6.0f, cfg.letterSpacing));
        cfg.letterSpacing = 1.5;
        assertEquals(7.5f, FontTweaksLetterSpacing.adjustedAdvance(6.0f, cfg.letterSpacing));
        assertEquals(5.5f, FontTweaksLetterSpacing.adjustedAdvance(4.0f, cfg.letterSpacing));
        // The same modified glyph advance is used for width and pen progression.
        assertEquals(22.5f, 3 * FontTweaksLetterSpacing.adjustedAdvance(6.0f, cfg.letterSpacing));
        assertEquals(9, cfg.lineStep(9));
        assertEquals(1.0, cfg.globalLineSpacing);
        assertEquals(1.0, cfg.wrappedLineSpacing);
    }

    @Test
    void negativeTrackingIsSupportedAndGlyphAdvancesRemainPositive() {
        assertEquals(4.0f, FontTweaksLetterSpacing.adjustedAdvance(6.0f, -2.0));
        assertEquals(0.5f, FontTweaksLetterSpacing.adjustedAdvance(1.0f, -2.0));
        assertEquals(6.0f, FontTweaksLetterSpacing.adjustedAdvance(6.0f, Double.NaN));
    }

    @Test
    void persistsPreciseFractionalTrackingAndSafeBounds() {
        Gson gson = new Gson();
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.letterSpacing = 0.6666666666666667;
        FontTweaksSettings copy = gson.fromJson(gson.toJson(cfg), FontTweaksSettings.class);
        assertEquals(0.6666666666666667, copy.letterSpacing);
        cfg.letterSpacing = 999;
        cfg.sanitize();
        assertEquals(4.0, cfg.letterSpacing);
        cfg.letterSpacing = -999;
        cfg.sanitize();
        assertEquals(-2.0, cfg.letterSpacing);
        cfg.letterSpacing = Double.NaN;
        cfg.sanitize();
        assertEquals(1.0, cfg.letterSpacing);
    }

    @Test
    void pixelProcessingNowDefaultsToOneWithoutMigratingExistingConfigs() {
        Gson gson = new Gson();
        assertEquals(1, new FontTweaksSettings().pixelBlockSize);
        assertEquals(1, gson.fromJson("{}", FontTweaksSettings.class).pixelBlockSize);
        assertEquals(2, gson.fromJson("{\"pixelBlockSize\":2}", FontTweaksSettings.class).pixelBlockSize);
        FontTweaksSettings invalid = gson.fromJson("{\"pixelBlockSize\":-1}", FontTweaksSettings.class);
        invalid.sanitize();
        assertEquals(1, invalid.pixelBlockSize);
    }
}
