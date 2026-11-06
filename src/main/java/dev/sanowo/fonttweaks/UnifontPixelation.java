package dev.sanowo.fonttweaks;

import java.nio.IntBuffer;

/** Coarsens the existing bitmap in place; dimensions and glyph metrics never change. */
public final class UnifontPixelation {
    private UnifontPixelation() {}

    public static IntBuffer apply(IntBuffer pixels, int width, int height, int blockSize) {
        if (blockSize <= 1) return pixels;
        if (width <= 0 || height <= 0 || (long) width * height != pixels.remaining()) {
            throw new IllegalArgumentException("Bitmap dimensions do not match buffer");
        }
        int start = pixels.position();
        // Majority coverage, with ties filled, retains binary opaque/transparent
        // pixels. Edge blocks are clipped to the original bitmap rectangle.
        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                int endX = Math.min(width, x + blockSize);
                int endY = Math.min(height, y + blockSize);
                int ink = 0;
                for (int yy = y; yy < endY; yy++) {
                    for (int xx = x; xx < endX; xx++) {
                        if (pixels.get(start + yy * width + xx) != 0) ink++;
                    }
                }
                int value = ink * 2 >= (endX - x) * (endY - y) ? -1 : 0;
                for (int yy = y; yy < endY; yy++) {
                    for (int xx = x; xx < endX; xx++) {
                        pixels.put(start + yy * width + xx, value);
                    }
                }
            }
        }
        return pixels;
    }
}
