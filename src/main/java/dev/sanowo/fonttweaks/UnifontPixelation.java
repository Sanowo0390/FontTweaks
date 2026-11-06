package dev.sanowo.fonttweaks;

/** Half-display-pixel raster: 2*GUI oversampling keeps original glyph dimensions. */
public final class UnifontPixelation {
    private UnifontPixelation() {}

    public static int[] rasterize(int[] rows, int left, int width, int guiScale, int screenPixels) {
        if (rows.length != 16 || width <= 0 || guiScale < 1 || screenPixels < 1) {
            throw new IllegalArgumentException("Invalid glyph/grid dimensions");
        }
        int w = width * guiScale, h = 16 * guiScale;
        int block = screenPixels * 2;
        int[] result = new int[w * h];
        for (int y = 0; y < h; y += block) {
            for (int x = 0; x < w; x += block) {
                int endX = Math.min(w, x + block), endY = Math.min(h, y + block);
                int ink = 0;
                for (int yy = y; yy < endY; yy++) {
                    for (int xx = x; xx < endX; xx++) {
                        int bit = 31 - left - xx / guiScale;
                        if (bit >= 0 && bit < 32 && ((rows[yy / guiScale] >>> bit) & 1) != 0) ink++;
                    }
                }
                // Keep coverage as alpha instead of discarding minority strokes.
                int area = (endX - x) * (endY - y);
                int alpha = ink == 0 ? 0 : Math.max(1, (ink * 255 + area / 2) / area);
                int value = alpha == 0 ? 0 : (alpha << 24) | 0xFFFFFF;
                for (int yy = y; yy < endY; yy++) {
                    for (int xx = x; xx < endX; xx++) result[yy * w + xx] = value;
                }
            }
        }
        return result;
    }
}
