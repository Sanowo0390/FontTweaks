package dev.sanowo.fonttweaks;

import java.nio.IntBuffer;

/**
 * Applies blocky, nearest-neighbour-style posterization to the ORIGINAL
 * 16-pixel-high Unihex glyph bitmap, preserving its bitmap dimensions,
 * advance, bearing, font size, and layout.
 *
 * Called on the same CPU IntBuffer used for uploading the glyph to the atlas.
 * No shader, framebuffer or whole-GUI scaling is involved.
 */
public final class PixelGlyphProcessor {
    private PixelGlyphProcessor() {}

    /**
     * Pixelates a freshly unpacked Unihex glyph, in place.
     *
     * @param pixels 16-row glyph bitmap; each int is one source texel
     * @param width the actual cropped bitmap width in texels (8/16/24/32 max)
     * @param blockSize 2, 3 or 4
     */
    public static void pixelate(IntBuffer pixels, int width, int blockSize) {
        final int height = 16;
        if (width <= 0 || width > 32 || blockSize < 2 || blockSize > 4) return;
        final int count = width * height;

        // The vanilla unpacker fills a buffer with relative puts. Respect
        // its current position and do not change position, limit or mark.
        final int end = pixels.position();
        int start = end - count;
        if (start < 0 || (long) start + count > pixels.limit()) {
            // Support an absolute-put or already-flipped buffer defensively.
            if (end == 0 && pixels.limit() >= count) {
                start = 0;
            } else {
                return;
            }
        }

        // Make sure all reads come from the original glyph, not pixels
        // overwritten by an earlier cell.
        final int[] source = new int[count];
        for (int i = 0; i < count; ++i) {
            source[i] = pixels.get(start + i);
        }

        for (int by = 0; by < height; by += blockSize) {
            int bh = Math.min(blockSize, height - by);
            for (int bx = 0; bx < width; bx += blockSize) {
                int bw = Math.min(blockSize, width - bx);
                int inkCount = 0;
                int inkValue = 0;
                for (int dy = 0; dy < bh; ++dy) {
                    for (int dx = 0; dx < bw; ++dx) {
                        int color = source[(by + dy) * width + bx + dx];
                        if (color != 0) {
                            ++inkCount;
                            inkValue = color;
                        }
                    }
                }

                // Preserve thin Unifont strokes using a 25%-coverage cutoff
                // instead of majority vote, which erases small kanji strokes.
                final int threshold = Math.max(1, (bw * bh + 3) / 4);
                final int result = inkCount >= threshold ? inkValue : 0;
                for (int dy = 0; dy < bh; ++dy) {
                    for (int dx = 0; dx < bw; ++dx) {
                        pixels.put(start + (by + dy) * width + bx + dx, result);
                    }
                }
            }
        }
    }
}
