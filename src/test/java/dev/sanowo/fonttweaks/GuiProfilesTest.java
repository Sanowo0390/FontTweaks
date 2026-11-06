package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regression tests for the GUI-size-specific pixel and font configuration. */
class GuiProfilesTest {
    @Test
    void eachGuiSizeHasIndependentPixelAndFontSettings() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.setProfilePixelSize(2, 1);
        cfg.setProfilePixelSize(3, 2);
        cfg.setProfilePixelSize(4, 4);
        cfg.setProfileExistingFont(3, true);
        assertEquals(1, cfg.profileFor(2).pixelBlockSize);
        assertEquals(2, cfg.profileFor(3).pixelBlockSize);
        assertEquals(4, cfg.profileFor(4).pixelBlockSize);
        assertFalse(cfg.profileFor(2).useExistingFont);
        assertTrue(cfg.profileFor(3).useExistingFont);
        assertFalse(cfg.profileFor(4).useExistingFont);
        assertSame(FontTweaksFonts.PIXELMPLUS,
                FontTweaksFonts.select(FontDescription.DEFAULT, '語', cfg, 3, false));
        assertSame(FontDescription.DEFAULT,
                FontTweaksFonts.select(FontDescription.DEFAULT, '語', cfg, 2, false));
    }

    @Test
    void threePixelsAndExistingFontAlwaysUsesMisakiForEveryGuiSize() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        for (int gui = 1; gui <= 7; gui++) {
            cfg.setProfilePixelSize(gui, 3);
            cfg.setProfileExistingFont(gui, true);
        }
        for (int gui = 1; gui <= 7; gui++) {
            assertSame(FontTweaksFonts.MISAKI,
                    FontTweaksFonts.select(FontDescription.DEFAULT, 'あ', cfg, gui, false));
            assertSame(FontTweaksFonts.MISAKI,
                    FontTweaksFonts.select(FontDescription.DEFAULT, '日', cfg, gui, true));
            // An explicitly selected font and a blacklisted glyph are respected.
            assertSame(FontDescription.DEFAULT,
                    FontTweaksFonts.select(FontDescription.DEFAULT, 'A', cfg, gui, false));
        }
        cfg.replaceEnglish = true;
        assertSame(FontTweaksFonts.MISAKI,
                FontTweaksFonts.select(FontDescription.DEFAULT, 'A', cfg, 4, false));
        cfg.blacklistedEntries.add("日");
        assertSame(FontDescription.DEFAULT,
                FontTweaksFonts.select(FontDescription.DEFAULT, '日', cfg, 4, false));
        FontDescription.Resource custom = new FontDescription.Resource(
                Identifier.fromNamespaceAndPath("test", "icons"));
        assertSame(custom, FontTweaksFonts.select(custom, '日', cfg, 3, false));
    }

    @Test
    void guiFourAndAboveShareOneExplicitTwoPixelProfile() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.setProfilePixelSize(4, 2);
        cfg.setProfileExistingFont(4, true);
        assertSame(cfg.profileFor(4), cfg.profileFor(5));
        assertSame(cfg.profileFor(4), cfg.profileFor(6));
        assertSame(cfg.profileFor(4), cfg.profileFor(20));
        assertEquals(2, cfg.profileFor(6).pixelBlockSize);
        assertEquals(2, cfg.effectivePixelBlockSize(6));
        assertSame(FontTweaksFonts.PIXELMPLUS,
                FontTweaksFonts.select(FontDescription.DEFAULT, '語', cfg, 6, false));
    }

    @Test
    void guiSpecificNativeAndProcessedRasterSignaturesAreIndependent() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.setProfilePixelSize(2, 1);
        cfg.setProfilePixelSize(3, 2);
        cfg.setProfilePixelSize(4, 1);
        assertEquals(0, cfg.pixelRasterSignature(2));
        assertEquals(2, cfg.pixelRasterSignature(3));
        assertEquals(0, cfg.pixelRasterSignature(4));
        cfg.setProfilePixelSize(4, 3);
        assertEquals(3, cfg.pixelRasterSignature(4));
    }

    @Test
    void oldGuiFiveProfileMigratesToFourWhenNoFourExists() {
        Gson gson = new Gson();
        FontTweaksSettings legacy = gson.fromJson(
                "{\"guiScaleProfiles\":{\"5\":{\"pixelBlockSize\":3,\"useExistingFont\":true," +
                "\"unifontProcessing\":false,\"replaceEnglish\":true}}}",
                FontTweaksSettings.class);
        legacy.sanitize();
        assertEquals(1, legacy.guiScaleProfiles.size());
        assertTrue(legacy.guiScaleProfiles.containsKey("4"));
        assertFalse(legacy.guiScaleProfiles.containsKey("5"));
        assertSame(legacy.profileFor(4), legacy.profileFor(6));
        assertEquals(3, legacy.profileFor(4).pixelBlockSize);
        assertTrue(legacy.profileFor(4).useExistingFont);
        assertFalse(legacy.unifontProcessingAt(7));
        assertTrue(legacy.replaceEnglishAt(5));
    }

    @Test
    void oldGuiFourProfileTakesPriorityOverConflictingGuiFive() {
        Gson gson = new Gson();
        FontTweaksSettings legacy = gson.fromJson(
                "{\"guiScaleProfiles\":{\"4\":{\"pixelBlockSize\":1,\"useExistingFont\":false}," +
                "\"5\":{\"pixelBlockSize\":3,\"useExistingFont\":true}}}",
                FontTweaksSettings.class);
        legacy.sanitize();
        assertFalse(legacy.guiScaleProfiles.containsKey("5"));
        assertEquals(1, legacy.profileFor(4).pixelBlockSize);
        assertEquals(1, legacy.profileFor(12).pixelBlockSize);
        assertFalse(legacy.profileFor(8).useExistingFont);
        assertEquals(4, FontTweaksSettings.profileScale(200));
    }

    @Test
    void gsonRoundTripAndOldFontChoiceMigrateSafely() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.pixelBlockSize = 1;
        cfg.resourcePackEnabled = true;
        cfg.resourcePackFont = "misaki";
        assertFalse(cfg.hasGuiProfiles());

        // Merely opening the settings screen converts legacy global fields
        // into independent profiles, without losing an explicit Misaki choice.
        cfg.profileFor(2);
        cfg.profileFor(3);
        assertSame(FontTweaksFonts.MISAKI,
                FontTweaksFonts.select(FontDescription.DEFAULT, '日', cfg, 3, false));
        cfg.setProfilePixelSize(3, 2);
        assertSame(FontTweaksFonts.PIXELMPLUS,
                FontTweaksFonts.select(FontDescription.DEFAULT, '日', cfg, 3, false));
        cfg.setProfilePixelSize(2, 3);
        assertSame(FontTweaksFonts.MISAKI,
                FontTweaksFonts.select(FontDescription.DEFAULT, '日', cfg, 2, false));

        Gson gson = new Gson();
        FontTweaksSettings restored = gson.fromJson(gson.toJson(cfg), FontTweaksSettings.class);
        restored.sanitize();
        assertEquals(3, restored.profileFor(2).pixelBlockSize);
        assertEquals(2, restored.profileFor(3).pixelBlockSize);
        assertTrue(restored.profileFor(2).useExistingFont);
        assertTrue(restored.profileFor(3).useExistingFont);
        assertSame(FontTweaksFonts.MISAKI,
                FontTweaksFonts.select(FontDescription.DEFAULT, '日', restored, 2, false));
        assertSame(FontTweaksFonts.PIXELMPLUS,
                FontTweaksFonts.select(FontDescription.DEFAULT, '日', restored, 3, false));
    }
}
