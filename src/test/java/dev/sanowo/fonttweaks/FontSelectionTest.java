package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.UnbakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FontSelectionTest {
    @Test void packOffBlocksBothFontsIncludingAutomaticMisakiAtEveryGrid() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.replaceEnglish = true;
        for (String font : new String[]{"pixelmplus12", "misaki", "auto"}) {
            cfg.resourcePackFont = font;
            for (int gui = 1; gui <= 4; gui++) {
                for (int pixels = 1; pixels <= 4; pixels++) {
                    cfg.pixelBlockSize = pixels;
                    assertNull(FontTweaksFonts.activePackFont(cfg, gui, false));
                    assertSame(FontDescription.DEFAULT,
                            FontTweaksFonts.select(FontDescription.DEFAULT, '日', cfg, gui, false));
                    assertSame(FontDescription.DEFAULT,
                            FontTweaksFonts.select(FontDescription.DEFAULT, 'A', cfg, gui, false));
                }
            }
        }
    }

    @Test void enabledPackHonorsFontChoiceAndGatesAutoMode() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        cfg.resourcePackFont = "misaki";
        assertSame(FontTweaksFonts.MISAKI, select('日', cfg));
        cfg.resourcePackFont = "auto";
        cfg.pixelBlockSize = 2;
        assertSame(FontTweaksFonts.PIXELMPLUS, select('日', cfg));
        cfg.pixelBlockSize = 3;
        assertSame(FontTweaksFonts.MISAKI, select('日', cfg));
        cfg.resourcePackEnabled = false;
        assertSame(FontDescription.DEFAULT, select('日', cfg));
    }

    @Test void fontChoiceCyclesAndPersistsWithSafeDefaults() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.cycleResourcePackFont();
        assertEquals("misaki", cfg.resourcePackFont);
        assertEquals("misaki", new Gson().fromJson(new Gson().toJson(cfg), FontTweaksSettings.class).resourcePackFont);
        cfg.cycleResourcePackFont();
        assertEquals("auto", cfg.resourcePackFont);
        cfg.cycleResourcePackFont();
        assertEquals("pixelmplus12", cfg.resourcePackFont);
        cfg.resourcePackFont = null;
        cfg.sanitize();
        assertEquals("pixelmplus12", cfg.resourcePackFont);
    }

    private FontDescription select(int cp, FontTweaksSettings cfg) {
        return FontTweaksFonts.select(FontDescription.DEFAULT, cp, cfg, 3, false);
    }

    @Test void pixelMplusIsIndependentOfPixelProcessingAndEnglishGrid() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        for (boolean pixel : new boolean[]{false, true}) {
            cfg.pixelationEnabled = pixel;
            for (int gui = 1; gui <= 4; gui++) {
                assertSame(FontTweaksFonts.PIXELMPLUS,
                        FontTweaksFonts.select(FontDescription.DEFAULT, '語', cfg, gui, true));
            }
        }
    }

    @Test void englishNumbersAndSymbolsKeepDefaultUnlessEnabled() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        for (int cp : new int[]{'A', 'z', '0', '!', 0xE9, 0xFF21}) {
            assertSame(FontDescription.DEFAULT, select(cp, cfg));
            cfg.replaceEnglish = true;
            assertSame(FontTweaksFonts.PIXELMPLUS, select(cp, cfg));
            cfg.replaceEnglish = false;
        }
        assertSame(FontTweaksFonts.PIXELMPLUS, select('あ', cfg));
    }

    @Test void blacklistWinsForJapaneseEnglishAndSupplementaryCharacters() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        cfg.replaceEnglish = true;
        cfg.blacklistedCharacters = "語A★\uD83D\uDE00";
        for (int cp : cfg.blacklistedCharacters.codePoints().toArray()) {
            assertTrue(cfg.isBlacklisted(cp));
            assertSame(FontDescription.DEFAULT, select(cp, cfg));
        }
        assertSame(FontTweaksFonts.PIXELMPLUS, select('日', cfg));
    }

    @Test void disablingPackAlwaysReturnsToOriginalFontWithoutChangingSize() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        cfg.globalScale = 1.25;
        assertSame(FontTweaksFonts.PIXELMPLUS, select('日', cfg));
        cfg.resourcePackEnabled = false;
        cfg.pixelBlockSize = 2;
        assertSame(FontDescription.DEFAULT, select('日', cfg));
        cfg.pixelBlockSize = 3;
        assertSame(FontDescription.DEFAULT, select('日', cfg));
        cfg.pixelationEnabled = false;
        assertSame(FontDescription.DEFAULT, select('日', cfg));
        assertEquals(1.25f, cfg.scaleFor(true));
    }

    @Test void customFontIdsArePreserved() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        cfg.replaceEnglish = true;
        FontDescription custom = new FontDescription.Resource(Identifier.fromNamespaceAndPath("test", "icons"));
        assertSame(custom, FontTweaksFonts.select(custom, '日', cfg, 3, false));
    }

    @Test void fontAndExclusionSettingsRoundTripAndOldConfigDefaultsRemainSafe() {
        Gson gson = new Gson();
        FontTweaksSettings old = gson.fromJson("{\"pixelBlockSize\":4}", FontTweaksSettings.class);
        old.sanitize();
        assertFalse(old.resourcePackEnabled);
        assertFalse(old.replaceEnglish);
        assertEquals("", old.blacklistedCharacters);
        old.resourcePackEnabled = true;
        old.replaceEnglish = true;
        old.blacklistedCharacters = "！？★\uD83D\uDE00";
        FontTweaksSettings restored = gson.fromJson(gson.toJson(old), FontTweaksSettings.class);
        assertTrue(restored.resourcePackEnabled);
        assertTrue(restored.replaceEnglish);
        assertEquals(old.blacklistedCharacters, restored.blacklistedCharacters);
        assertEquals(4, restored.pixelBlockSize);
    }

    @Test void excludedGlyphSkipsBitmapProcessingAndRestoresContextAfterFailure() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        String originalBlacklist = cfg.blacklistedCharacters;
        try {
            cfg.blacklistedCharacters = "★";
            UnbakedGlyph probe = new UnbakedGlyph() {
                public GlyphInfo info() { return null; }
                public BakedGlyph bake(Stitcher stitcher) {
                    assertTrue(GlyphProcessingContext.excluded());
                    throw new IllegalStateException("probe");
                }
            };
            assertFalse(GlyphProcessingContext.excluded());
            assertThrows(IllegalStateException.class, () -> new CharacterGlyph(probe, '★').bake(null));
            assertFalse(GlyphProcessingContext.excluded());
        } finally { cfg.blacklistedCharacters = originalBlacklist; }
    }

    @Test void blacklistIsReadWhenGlyphIsBakedAndCanBeRemovedAgain() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        String originalBlacklist = cfg.blacklistedCharacters;
        boolean[] seen = {false};
        UnbakedGlyph probe = new UnbakedGlyph() {
            public GlyphInfo info() { return null; }
            public BakedGlyph bake(Stitcher stitcher) {
                seen[0] = GlyphProcessingContext.excluded();
                return null;
            }
        };
        CharacterGlyph glyph = new CharacterGlyph(probe, '語');
        try {
            cfg.blacklistedCharacters = "";
            glyph.bake(null);
            assertFalse(seen[0]);
            cfg.blacklistedCharacters = "語";
            glyph.bake(null);
            assertTrue(seen[0]);
            cfg.blacklistedCharacters = "";
            glyph.bake(null);
            assertFalse(seen[0]);
            assertFalse(GlyphProcessingContext.excluded());
        } finally { cfg.blacklistedCharacters = originalBlacklist; }
    }
}
