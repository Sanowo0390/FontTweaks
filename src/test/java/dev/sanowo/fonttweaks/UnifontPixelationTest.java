package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import java.nio.IntBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnifontPixelationTest {
    @Test void rasterResolutionChangesWithoutChangingGlyphBounds() {
        com.mojang.blaze3d.font.GlyphBitmap original = new com.mojang.blaze3d.font.GlyphBitmap() {
            public int getPixelWidth() { return 13; }
            public int getPixelHeight() { return 16; }
            public float getOversample() { return 2; }
            public boolean isColored() { return true; }
            public void upload(int x, int y, com.mojang.renderpearl.api.textures.GpuTexture t) {
                fail("No GPU upload expected in metrics test");
            }
        };
        for (int gui = 1; gui <= 4; gui++) {
            PixelatedGlyphBitmap processed = new PixelatedGlyphBitmap(original, new int[16], 0, gui, 3);
            assertEquals(original.getLeft(), processed.getLeft());
            assertEquals(original.getRight(), processed.getRight());
            assertEquals(original.getTop(), processed.getTop());
            assertEquals(original.getBottom(), processed.getBottom());
            assertEquals(6.5, processed.getPixelWidth() / (double) processed.getOversample());
            assertEquals(8.0, processed.getPixelHeight() / (double) processed.getOversample());
        }
    }

    @Test void thinMinorityStrokesSurvive() {
        int[] rows = new int[16];
        rows[0] = 0x80000000;
        int[] result = UnifontPixelation.rasterize(rows, 0, 16, 3, 3);
        // One lit source pixel in a 2x2 source block used to disappear.
        assertEquals(255, result[0] >>> 24);
        assertEquals(result[0], result[5 * 48 + 5]);
        assertEquals(0, result[6]);
    }

    @Test void requestedDotSizeUsesDisplayPixelsAtEveryGuiScale() {
        int[] rows = new int[16];
        rows[0] = 0x80000000;
        for (int gui = 1; gui <= 4; gui++) {
            for (int target = 1; target <= 3; target++) {
                int[] result = UnifontPixelation.rasterize(rows, 0, 16, gui, target);
                assertEquals(16 * gui * 16 * gui, result.length);
                int block = target * 2;
                for (int y = 0; y < block; y++) {
                    for (int x = 0; x < block; x++) assertEquals(result[0], result[y * 16 * gui + x]);
                }
                assertTrue((result[0] >>> 24) > 0);
                // Output uses 2*gui texels per GUI unit: exactly target screen px.
                assertEquals(target, block / (2.0 * gui) * gui, 1e-12);
            }
        }
    }

    @Test void croppedOddWidthAndBlankGlyphAreHandled() {
        int[] rows = new int[16];
        assertArrayEquals(new int[7 * 3 * 16 * 3],
                UnifontPixelation.rasterize(rows, 5, 7, 3, 2));
        rows[15] = 1 << (31 - 11);
        int[] raster = UnifontPixelation.rasterize(rows, 5, 7, 3, 2);
        assertTrue((raster[raster.length - 1] >>> 24) > 0);
    }

    @Test void everyOutputPixelIsOpaqueOrTransparent() {
        int[] rows = new int[16];
        for (int y = 0; y < 16; y++) rows[y] = 0xAA55AA55;
        for (int gui = 1; gui <= 4; gui++) {
            for (int target = 1; target <= 3; target++) {
                for (int pixel : UnifontPixelation.rasterize(rows, 0, 16, gui, target)) {
                    assertTrue(pixel == 0 || pixel == -1);
                }
            }
        }
    }

    @Test void invalidDimensionsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> UnifontPixelation.rasterize(new int[15], 0, 16, 3, 3));
    }

    @Test void effectToggleAndRoughnessDoNotChangeSizeOrSpacing() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.globalScale = 1.25;
        cfg.screenScale = 1.2;
        for (boolean enabled : new boolean[]{true, false}) {
            cfg.pixelationEnabled = enabled;
            for (int block = 1; block <= 3; block++) {
                cfg.pixelBlockSize = block;
                assertEquals(1.5f, cfg.scaleFor(true));
                assertEquals(1.25f, cfg.scaleFor(false));
                assertEquals(1.0, cfg.wrappedLineSpacing);
            }
        }
        FontTweaksSettings defaults = new FontTweaksSettings();
        assertEquals(1.0f, defaults.scaleFor(true));
        assertEquals(1.0f, defaults.scaleFor(false));
    }

    @Test void oldSettingsMigrateWithoutOverridingSavedSizes() {
        Gson gson = new Gson();
        FontTweaksSettings cfg = gson.fromJson(
                "{\"pixelPerfectMode\":false,\"pixelsPerFontDot\":3,\"globalScale\":1.25}",
                FontTweaksSettings.class);
        cfg.sanitize();
        assertFalse(cfg.pixelationEnabled);
        assertEquals(3, cfg.pixelBlockSize);
        assertEquals(1.25f, cfg.scaleFor(true));
        String saved = gson.toJson(cfg);
        assertTrue(saved.contains("pixelationEnabled"));
        assertFalse(saved.contains("pixelPerfectMode"));
    }
}
