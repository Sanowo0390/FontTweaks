package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import net.minecraft.network.chat.FontDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FontTweaksPresetsTest {
    private static final int[] GUI_SCALES = {1, 2, 3, 4, 5, 6, 7};

    private static FontDescription selected(FontTweaksSettings cfg, int guiScale, int codepoint) {
        return FontTweaksFonts.select(FontDescription.DEFAULT, codepoint, cfg, guiScale, false);
    }

    @Test
    void correctionOnlyEditsGuiOneAndThree() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.replaceEnglish = true;
        FontTweaksPresets.apply(cfg, FontTweaksPresets.PIXEL_CORRECTION);
        assertEquals(FontTweaksPresets.PIXEL_CORRECTION, cfg.activePreset);
        assertEquals(1, cfg.profileFor(1).effectivePixels());
        assertEquals(2, cfg.profileFor(3).effectivePixels());
        assertTrue(cfg.unifontProcessingAt(1));
        assertTrue(cfg.unifontProcessingAt(3));
        assertEquals(1, cfg.pixelRasterSignature(1));
        assertEquals(2, cfg.pixelRasterSignature(3));
        for (int gui : new int[]{2, 4, 5, 6, 7}) {
            assertEquals(0, cfg.pixelRasterSignature(gui), "GUI " + gui + " must remain native");
            assertFalse(cfg.profileFor(gui).useExistingFont);
            assertFalse(cfg.unifontProcessingAt(gui));
        }
        for (int gui : GUI_SCALES) {
            assertSame(FontDescription.DEFAULT, selected(cfg, gui, '日'));
            assertFalse(cfg.replaceEnglishAt(gui));
            assertSame(FontDescription.DEFAULT, selected(cfg, gui, 'A'));
        }
    }

    @Test
    void correctionAndFontsUsesMisakiAtGuiOnePixelMplusAtThreeAndNativeAtFour() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        FontTweaksPresets.apply(cfg, FontTweaksPresets.PIXEL_AND_FONTS);
        assertSame(FontTweaksFonts.MISAKI, selected(cfg, 1, '日'));
        assertEquals(3, cfg.profileFor(1).pixelBlockSize);
        assertTrue(cfg.unifontProcessingAt(1));
        assertSame(FontTweaksFonts.PIXELMPLUS, selected(cfg, 3, '日'));
        assertEquals(2, cfg.profileFor(3).pixelBlockSize);
        assertTrue(cfg.unifontProcessingAt(3));
        for (int gui : new int[]{2, 4, 5, 6}) {
            assertSame(FontDescription.DEFAULT, selected(cfg, gui, '日'));
            assertFalse(cfg.unifontProcessingAt(gui));
            assertEquals(0, cfg.pixelRasterSignature(gui));
        }
        for (int gui : GUI_SCALES) {
            assertFalse(cfg.replaceEnglishAt(gui));
            assertSame(FontDescription.DEFAULT, selected(cfg, gui, 'A'));
        }
    }

    @Test
    void misakiAllProfilesAlwaysSelectsMisakiAndThreeDotsWithoutChangingLatin() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        FontTweaksPresets.apply(cfg, FontTweaksPresets.ALWAYS_MISAKI);
        for (int gui : GUI_SCALES) {
            assertEquals(3, cfg.profileFor(gui).effectivePixels());
            assertTrue(cfg.profileFor(gui).useExistingFont);
            assertTrue(cfg.unifontProcessingAt(gui));
            assertFalse(cfg.replaceEnglishAt(gui));
            assertSame(FontTweaksFonts.MISAKI, selected(cfg, gui, '日'));
            assertSame(FontDescription.DEFAULT, selected(cfg, gui, 'A'));
        }
        cfg.replaceEnglish = true; // Legacy global option cannot override a preset.
        assertSame(FontDescription.DEFAULT, selected(cfg, 3, 'A'));
    }

    @Test
    void customProfileEditsChangeOnlyTheirTargetAndClearPresetLabel() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        FontTweaksPresets.apply(cfg, FontTweaksPresets.ALWAYS_MISAKI);
        cfg.setProfileUnifontProcessing(4, false);
        assertEquals(FontTweaksPresets.CUSTOM, cfg.activePreset);
        assertFalse(cfg.unifontProcessingAt(4));
        assertTrue(cfg.unifontProcessingAt(3));
        assertEquals(0, cfg.pixelRasterSignature(4));
        assertEquals(3, cfg.pixelRasterSignature(3));

        cfg.setProfileReplaceEnglish(4, true);
        assertSame(FontTweaksFonts.MISAKI, selected(cfg, 4, 'A'));
        assertSame(FontDescription.DEFAULT, selected(cfg, 3, 'A'));
        cfg.setProfileExistingFont(4, false);
        assertSame(FontDescription.DEFAULT, selected(cfg, 4, '日'));
        assertSame(FontTweaksFonts.MISAKI, selected(cfg, 3, '日'));
        cfg.setProfilePixelSize(4, 2);
        assertEquals(2, cfg.profileFor(4).pixelBlockSize);
    }

    @Test
    void presetRoundTripsWithGuiOverridesAndOldProfilesInheritOldFlags() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        FontTweaksPresets.apply(cfg, FontTweaksPresets.PIXEL_AND_FONTS);
        Gson gson = new Gson();
        FontTweaksSettings restored = gson.fromJson(gson.toJson(cfg), FontTweaksSettings.class);
        restored.sanitize();
        assertEquals(FontTweaksPresets.PIXEL_AND_FONTS, restored.activePreset);
        assertSame(FontTweaksFonts.MISAKI, selected(restored, 1, '日'));
        assertSame(FontTweaksFonts.PIXELMPLUS, selected(restored, 3, '日'));
        assertFalse(restored.replaceEnglishAt(1));
        assertFalse(restored.unifontProcessingAt(4));

        FontTweaksSettings legacy = gson.fromJson(
            "{\"pixelationEnabled\":false,\"replaceEnglish\":true," +
            "\"guiScaleProfiles\":{\"3\":{\"pixelBlockSize\":2,\"useExistingFont\":true}}}",
            FontTweaksSettings.class);
        legacy.sanitize();
        assertEquals(FontTweaksPresets.CUSTOM, legacy.activePreset);
        assertFalse(legacy.unifontProcessingAt(3));
        assertTrue(legacy.replaceEnglishAt(3));
        assertSame(FontTweaksFonts.PIXELMPLUS, selected(legacy, 3, 'A'));
    }
}
