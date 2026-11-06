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
        if (block > guiScale) {
            // Reduction: assign each original ink pixel to ONE output cell by
            // its center. Overlap/max coverage duplicated a single thin stroke
            // into multiple cells and closed the spaces between strokes.
            for (int sy = 0; sy < 16; sy++) {
                for (int sx = 0; sx < width; sx++) {
                    int bit = 31 - left - sx;
                    if (bit < 0 || bit >= 32 || ((rows[sy] >>> bit) & 1) == 0) continue;
                    int x = ((2 * sx + 1) * guiScale / (2 * block)) * block;
                    int y = ((2 * sy + 1) * guiScale / (2 * block)) * block;
                    for (int yy = y; yy < Math.min(h, y + block); yy++) {
                        for (int xx = x; xx < Math.min(w, x + block); xx++) {
                            result[yy * w + xx] = -1;
                        }
                    }
                }
            }
            return result;
        }
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
                // Binary max coverage: thin strokes stay opaque, with no gray/alpha fringe.
                int value = ink == 0 ? 0 : -1;
                for (int yy = y; yy < endY; yy++) {
                    for (int xx = x; xx < endX; xx++) result[yy * w + xx] = value;
                }
            }
        }
        return result;
    }
}
