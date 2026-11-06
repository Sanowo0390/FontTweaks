package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.network.chat.FontDescription;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BlacklistEntriesTest {
    @Test void oldStringMigratesToIndividualUnicodeEntriesAndIsNotSavedAgain() {
        Gson gson = new Gson();
        FontTweaksSettings cfg = gson.fromJson("{\"blacklistedCharacters\":\"！？😀\"}", FontTweaksSettings.class);
        cfg.sanitize();
        assertEquals(List.of("！", "？", "😀"), cfg.blacklistedEntries);
        assertNull(cfg.blacklistedCharacters);
        assertFalse(gson.toJson(cfg).contains("blacklistedCharacters"));
        assertEquals(cfg.blacklistedEntries, gson.fromJson(gson.toJson(cfg), FontTweaksSettings.class).blacklistedEntries);
    }

    @Test void addIsUniqueAndDeleteRemovesOnlyThatCharacter() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        assertTrue(cfg.addBlacklistCharacters("日語😀日"));
        assertEquals(List.of("日", "語", "😀"), cfg.blacklistedEntries);
        assertFalse(cfg.addBlacklistCharacters("日😀"));
        cfg.blacklistedEntries.remove(1);
        cfg.sanitize();
        assertEquals(List.of("日", "😀"), cfg.blacklistedEntries);
        assertFalse(cfg.isBlacklisted('語'));
        assertTrue(cfg.isBlacklisted(0x1F600));
    }

    @Test void listNormalizationIsIdempotentAndHandlesOldCombinedEntries() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.blacklistedEntries = java.util.Arrays.asList("AB", null, "A", "😀");
        cfg.sanitize(); cfg.sanitize();
        assertEquals(List.of("A", "B", "😀"), cfg.blacklistedEntries);
    }

    @Test void blacklistedFontFallsBackButItsUnifontBitmapStillGetsProcessed() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.resourcePackEnabled = true;
        cfg.addBlacklistCharacters("語");
        assertSame(FontDescription.DEFAULT, FontTweaksFonts.select(FontDescription.DEFAULT, '語', cfg, 3, false));
        GlyphBitmap original = new GlyphBitmap() {
            public int getPixelWidth() { return 16; }
            public int getPixelHeight() { return 16; }
            public float getOversample() { return 2; }
            public boolean isColored() { return true; }
            public void upload(int x, int y, GpuTexture texture) { fail("No GPU upload expected"); }
        };
        assertInstanceOf(PixelatedGlyphBitmap.class, PixelatedGlyphBitmap.process(original, new int[16], 0, 3, cfg));
        cfg.pixelationEnabled = false;
        assertSame(original, PixelatedGlyphBitmap.process(original, new int[16], 0, 3, cfg));
    }
}
