package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import java.nio.IntBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnifontPixelationTest {
    @Test void guiThreeOnePixelKeepsTheExistingRaster() {
        int[] rows = new int[16];
        rows[1] = 1 << 30;
        int[] result = UnifontPixelation.rasterize(rows, 0, 16, 3, 1);
        // Existing 0.4.4 raster fixture: four adjacent 1px cells, not one 2px cell.
        for (int y = 0; y < 48; y++) {
            for (int x = 0; x < 48; x++) {
                assertEquals(x >= 2 && x < 6 && y >= 2 && y < 6 ? -1 : 0, result[y * 48 + x]);
            }
        }
    }

    @Test void enclosedSourceGapDoesNotBecomeSolidAtGuiThreeTwoPixels() {
        int[] rows = new int[16];
        for (int y = 0; y < 4; y++) rows[y] = 0xF0000000;
        rows[1] &= ~(1 << 30);
        int[] result = UnifontPixelation.rasterize(rows, 0, 16, 3, 2);
        // Source hole x=1,y=1 collided with surrounding ink in the old center mapping.
        assertEquals(0, result[4 * 48 + 4]);
        assertEquals(-1, result[0]);
        assertEquals(-1, result[8 * 48 + 8]);
    }

    @Test void gapProtectionDoesNotEraseStraightThinStroke() {
        int[] rows = new int[16];
        for (int y = 0; y < 7; y++) rows[y] = (1 << 31) | (1 << 25);
        rows[1] = rows[3] = 0xFE000000;
        int[] result = UnifontPixelation.rasterize(rows, 0, 16, 3, 2);
        // The narrow source gap and the upper bar map to the same output row.
        // Keep both bars instead of deleting a thin stroke to force a gap.
        for (int x = 0; x < 20; x++) {
            assertEquals(-1, result[4 * 48 + x]);
            assertEquals(-1, result[8 * 48 + x]);
        }
    }

    @Test void guiThreeTwoPixelsDoesNotSpreadOneStrokeAcrossFourCells() {
        int[] rows = new int[16];
        rows[1] = 1 << 30;
        int[] result = UnifontPixelation.rasterize(rows, 0, 16, 3, 2);
        int count = 0;
        for (int pixel : result) if (pixel != 0) count++;
        // A source pixel straddles four 2px cells, but must fill just one.
        assertEquals(16, count);
        assertEquals(-1, result[4 * 48 + 4]);
        assertEquals(0, result[3 * 48 + 3]);
    }

    @Test void guiThreeTwoPixelsKeepsGapBetweenParallelStrokes() {
        int[] rows = new int[16];
        for (int y = 0; y < 16; y++) rows[y] = (1 << 30) | (1 << 27);
        int[] result = UnifontPixelation.rasterize(rows, 0, 16, 3, 2);
        // Centers land in cells x=4..7 and x=12..15; x=8..11 remains open.
        assertEquals(-1, result[4]);
        assertEquals(0, result[8]);
        assertEquals(-1, result[12]);
    }

    @Test void guiFourTwoPixelGridExactlyPreservesNativeGlyph() {
        int[] rows = new int[16];
        for (int y = 0; y < 16; y++) rows[y] = 0xA55AA55A ^ (y << 20);
        int[] output = UnifontPixelation.rasterize(rows, 3, 13, 4, 2);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 52; x++) {
                int expected = ((rows[y / 4] >>> (31 - 3 - x / 4)) & 1) == 0 ? 0 : -1;
                assertEquals(expected, output[y * 52 + x]);
            }
        }
    }

    @Test void fourPixelSettingSurvivesSerialization() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.pixelBlockSize = 4;
        cfg.sanitize();
        assertEquals(4, cfg.pixelBlockSize);
        FontTweaksSettings restored = new Gson().fromJson(new Gson().toJson(cfg), FontTweaksSettings.class);
        assertEquals(4, restored.pixelBlockSize);
    }

    @Test void autoModeIsPersistedAndProducesTheSameGui3AtlasAsManualTwoPx() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        assertEquals(1, cfg.pixelBlockSize); // New-install default remains 1px.
        assertEquals(1, cfg.effectivePixelBlockSize());

        cfg.pixelBlockSize = FontTweaksSettings.AUTO_PIXEL_SIZE;
        cfg.sanitize();
        assertEquals(FontTweaksSettings.AUTO_PIXEL_SIZE, cfg.pixelBlockSize);
        assertEquals(2, cfg.effectivePixelBlockSize());
        FontTweaksSettings restored = new Gson().fromJson(new Gson().toJson(cfg),
                FontTweaksSettings.class);
        restored.sanitize();
        assertEquals(FontTweaksSettings.AUTO_PIXEL_SIZE, restored.pixelBlockSize);
        assertEquals(2, restored.effectivePixelBlockSize());

        cfg.pixelBlockSize = 2;
        assertNotEquals(FontTweaksSettings.AUTO_PIXEL_SIZE, cfg.pixelBlockSize);
        assertEquals(restored.effectivePixelBlockSize(), cfg.effectivePixelBlockSize());
    }

    @Test void productionPixelationUsesFixedGuiThreeAtlasInsteadOfLiveGuiScale() {
        com.mojang.blaze3d.font.GlyphBitmap original = new com.mojang.blaze3d.font.GlyphBitmap() {
            public int getPixelWidth() { return 16; }
            public int getPixelHeight() { return 16; }
            public float getOversample() { return 2; }
            public boolean isColored() { return true; }
            public void upload(int x, int y, com.mojang.renderpearl.api.textures.GpuTexture t) {
                fail("Unexpected GPU call");
            }
        };
        FontTweaksSettings settings = new FontTweaksSettings();
        settings.pixelationEnabled = true;
        settings.pixelBlockSize = 2;
        com.mojang.blaze3d.font.GlyphBitmap processed =
                PixelatedGlyphBitmap.process(original, new int[16], 0, settings);
        assertEquals(3, PixelatedGlyphBitmap.ATLAS_RASTER_GUI_SCALE);
        assertEquals(48, processed.getPixelWidth());
        assertEquals(48, processed.getPixelHeight());
        assertEquals(6.0f, processed.getOversample());
        // No live GUI scale is accepted: a GUI scale change cannot change
        // these cached atlas dimensions or require a rebuild.
        settings.pixelationEnabled = false;
        assertSame(original, PixelatedGlyphBitmap.process(original, new int[16], 0, settings));
    }

    @Test void nativeGuiTwoAndFourAtOnePxAndGuiThreeCorrectionAlwaysFollowsProcessing() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.pixelBlockSize = 1;
        assertEquals(0, cfg.pixelRasterSignature(2));
        assertEquals(0, cfg.pixelRasterSignature(4));
        assertFalse(cfg.shouldPixelateAt(2));
        assertFalse(cfg.shouldPixelateAt(4));
        assertEquals(1, cfg.pixelRasterSignature(3));
        cfg.pixelBlockSize = 2;
        assertEquals(2, cfg.pixelRasterSignature(3));
        assertEquals(2, cfg.pixelRasterSignature(2));
        assertEquals(2, cfg.pixelRasterSignature(3));
        assertEquals(2, cfg.pixelRasterSignature(4));
        cfg.pixelationEnabled = false;
        assertEquals(0, cfg.pixelRasterSignature(3));
    }

    @Test void nativePixelCasesDoNotReplaceGlyphBitmapButGuiThreeTwoPxDoes() {
        com.mojang.blaze3d.font.GlyphBitmap original = new com.mojang.blaze3d.font.GlyphBitmap() {
            public int getPixelWidth() { return 16; }
            public int getPixelHeight() { return 16; }
            public float getOversample() { return 2; }
            public boolean isColored() { return true; }
            public void upload(int x, int y, com.mojang.renderpearl.api.textures.GpuTexture t) {
                fail("Unexpected GPU upload");
            }
        };
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.pixelBlockSize = 1;
        assertSame(original, PixelatedGlyphBitmap.process(original, new int[16], 0, cfg, 2));
        assertSame(original, PixelatedGlyphBitmap.process(original, new int[16], 0, cfg, 4));
        cfg.pixelBlockSize = 2;
        assertNotSame(original, PixelatedGlyphBitmap.process(original, new int[16], 0, cfg, 3));
        cfg.pixelationEnabled = false;
        assertSame(original, PixelatedGlyphBitmap.process(original, new int[16], 0, cfg, 3));
    }

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
            for (int target = 1; target <= 4; target++) {
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
        assertEquals(-1, raster[44 * 21 + 16]);
        assertEquals(0, raster[raster.length - 1]);
    }

    @Test void everyOutputPixelIsOpaqueOrTransparent() {
        int[] rows = new int[16];
        for (int y = 0; y < 16; y++) rows[y] = 0xAA55AA55;
        for (int gui = 1; gui <= 4; gui++) {
            for (int target = 1; target <= 4; target++) {
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
            for (int block = 1; block <= 4; block++) {
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

