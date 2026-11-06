package dev.sanowo.fonttweaks;

import org.junit.jupiter.api.Test;
import java.nio.IntBuffer;
import static org.junit.jupiter.api.Assertions.*;

class PixelGlyphProcessorTest {
    @Test
    void pixelatesGlyphBitmapWithoutResizingOrChangingBufferPosition() {
        IntBuffer bits = IntBuffer.allocate(16 * 16);
        bits.put(0, 0xFFFFFFFF);
        bits.position(16 * 16);

        PixelGlyphProcessor.pixelate(bits, 16, 2);

        assertEquals(16 * 16, bits.position());
        assertEquals(16 * 16, bits.limit());
        assertEquals(0xFFFFFFFF, bits.get(0));
        assertEquals(0xFFFFFFFF, bits.get(1));
        assertEquals(0xFFFFFFFF, bits.get(16));
        assertEquals(0xFFFFFFFF, bits.get(17));
        assertEquals(0, bits.get(2));
        assertEquals(0, bits.get(32));
    }

    @Test
    void usesCoverageThresholdToPreserveThinStrokes() {
        IntBuffer bits = IntBuffer.allocate(16 * 16);
        bits.put(0, 0xFF);
        bits.put(1, 0xFF);
        bits.put(16, 0xFF);
        bits.position(256);

        PixelGlyphProcessor.pixelate(bits, 16, 3);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                assertEquals(0xFF, bits.get(y * 16 + x));
            }
        }
        assertEquals(0, bits.get(3));
    }

    @Test
    void preservesOffsetAndPrefixOfPartiallyFilledBuffer() {
        IntBuffer bits = IntBuffer.allocate(5 + 8 * 16);
        bits.put(0, 42);
        bits.position(5);
        bits.put(0xFFFFFFFF);
        bits.position(5 + 8 * 16);

        PixelGlyphProcessor.pixelate(bits, 8, 2);
        assertEquals(42, bits.get(0));
        assertEquals(5 + 8 * 16, bits.position());
        assertEquals(0xFFFFFFFF, bits.get(5));
        assertEquals(0xFFFFFFFF, bits.get(6));
        assertEquals(0xFFFFFFFF, bits.get(5 + 8));
    }

    @Test
    void strengthAndInvalidInputsCannotWriteOutsideGlyph() {
        IntBuffer bits = IntBuffer.allocate(4 + 16 * 16);
        bits.put(0, 42);
        bits.put(3, 43);
        bits.position(4 + 16 * 16);
        PixelGlyphProcessor.pixelate(bits, 16, 4);
        PixelGlyphProcessor.pixelate(bits, 16, 0);
        PixelGlyphProcessor.pixelate(bits, 99, 3);
        assertEquals(42, bits.get(0));
        assertEquals(43, bits.get(3));
        assertEquals(4 + 16 * 16, bits.position());
    }
}
