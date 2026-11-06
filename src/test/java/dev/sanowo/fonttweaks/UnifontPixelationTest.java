package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import java.nio.IntBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnifontPixelationTest {
    @Test void oneLeavesEveryPixelUntouched() {
        int[] data = {-1, 0, -1, 0};
        UnifontPixelation.apply(IntBuffer.wrap(data), 2, 2, 1);
        assertArrayEquals(new int[]{-1, 0, -1, 0}, data);
    }

    @Test void twoUsesMajorityCoverageAndKeepsDimensions() {
        int[] data = {-1, 0, -1, 0, -1, 0, 0, 0};
        IntBuffer buffer = IntBuffer.wrap(data);
        UnifontPixelation.apply(buffer, 4, 2, 2);
        assertArrayEquals(new int[]{-1, -1, 0, 0, -1, -1, 0, 0}, data);
        assertEquals(0, buffer.position());
        assertEquals(8, buffer.limit());
    }

    @Test void threeClipsEdgesAndPreservesBufferOutsideBitmap() {
        int[] data = {123, -1, -1, -1, 0, -1, -1, 0, -1, 456};
        IntBuffer buffer = IntBuffer.wrap(data);
        buffer.position(1).limit(9);
        UnifontPixelation.apply(buffer, 4, 2, 3);
        assertArrayEquals(new int[]{123, -1, -1, -1, -1, -1, -1, -1, -1, 456}, data);
        assertEquals(1, buffer.position());
        assertEquals(9, buffer.limit());
    }

    @Test void bitmapDimensionsMustMatch() {
        assertThrows(IllegalArgumentException.class,
                () -> UnifontPixelation.apply(IntBuffer.allocate(7), 4, 2, 2));
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
