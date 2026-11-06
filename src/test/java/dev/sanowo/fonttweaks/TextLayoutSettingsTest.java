package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextLayoutSettingsTest {
    @Test void globalSpacingCombinesWithLocalSettingsAndChatWithoutChangingTextSize() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.globalLineSpacing = 1.5;
        cfg.wrappedLineSpacing = 1.2;
        cfg.tooltipLineSpacing = 0.8;
        assertEquals(1.8, cfg.wrappedSpacing(), 1e-12);
        assertEquals(1.2, cfg.tooltipSpacing(), 1e-12);
        assertEquals(14, cfg.lineStep(9));
        assertEquals(1.0, cfg.scaleFor(true));
        for (double vanilla : new double[]{0, 0.5, 1}) {
            assertEquals((vanilla + 1) * 1.5, cfg.chatSpacingOption(vanilla) + 1, 1e-12);
        }
    }

    @Test void allSizeChannelsWorkWithPixelMplusEnabledAndPixelProcessingOff() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        cfg.pixelationEnabled = false;
        cfg.globalScale = 1.2;
        cfg.screenScale = 1.25;
        cfg.hudScale = 0.5;
        cfg.buttonScale = 1.1;
        cfg.chatScale = 1.5;
        assertEquals(1.5, cfg.scaleFor(true), 1e-6);
        assertEquals(0.6, cfg.scaleFor(false), 1e-6);
        assertEquals(1.65, cfg.buttonTextScale(), 1e-12);
        assertEquals(1.8, cfg.chatTextScale(), 1e-12);
        cfg.resourcePackEnabled = false;
        assertEquals(1.65, cfg.buttonTextScale(), 1e-12);
    }

    @Test void spacingDefaultsAndPersistenceAreCompatible() {
        Gson gson = new Gson();
        FontTweaksSettings cfg = gson.fromJson("{}", FontTweaksSettings.class);
        assertEquals(1, cfg.globalLineSpacing);
        cfg.globalLineSpacing = 1.4;
        assertEquals(1.4, gson.fromJson(gson.toJson(cfg), FontTweaksSettings.class).globalLineSpacing);
        cfg.globalLineSpacing = Double.NaN;
        cfg.sanitize();
        assertEquals(1, cfg.globalLineSpacing);
    }
}

