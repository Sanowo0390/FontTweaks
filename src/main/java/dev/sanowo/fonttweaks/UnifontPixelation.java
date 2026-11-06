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
            preserveGaps(rows, left, width, guiScale, block, result);
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

    /** Restore source counters/gaps only where deleting a cell leaves its neighbors connected. */
    private static void preserveGaps(int[] rows, int left, int width, int guiScale,
                                     int block, int[] result) {
        int w = width * guiScale, h = 16 * guiScale;
        int columns = (w + block - 1) / block, lines = (h + block - 1) / block;
        boolean[] ink = new boolean[columns * lines];
        boolean[] gap = new boolean[ink.length];
        boolean[] protectedStroke = new boolean[ink.length];
        int[] support = new int[ink.length];
        for (int y = 0; y < lines; y++) {
            for (int x = 0; x < columns; x++) ink[y * columns + x] = result[y * block * w + x * block] != 0;
        }
        for (int sy = 0; sy < 16; sy++) {
            for (int sx = 0; sx < width; sx++) {
                int x = (2 * sx + 1) * guiScale / (2 * block);
                int y = (2 * sy + 1) * guiScale / (2 * block);
                int index = y * columns + x;
                if (sourceInk(rows, left, width, sx, sy)) {
                    support[index]++;
                    boolean l = sourceInk(rows, left, width, sx - 1, sy);
                    boolean r = sourceInk(rows, left, width, sx + 1, sy);
                    boolean u = sourceInk(rows, left, width, sx, sy - 1);
                    boolean d = sourceInk(rows, left, width, sx, sy + 1);
                    if ((l && r && !u && !d) || (u && d && !l && !r)) {
                        protectedStroke[index] = true;
                    }
                } else if ((sourceInk(rows, left, width, sx - 1, sy)
                        && sourceInk(rows, left, width, sx + 1, sy))
                        || (sourceInk(rows, left, width, sx, sy - 1)
                        && sourceInk(rows, left, width, sx, sy + 1))) {
                    // A narrow source gap, not arbitrary thinning of every stroke.
                    gap[index] = true;
                }
            }
        }
        java.util.List<Integer> candidates = new java.util.ArrayList<>();
        for (int i = 0; i < ink.length; i++) {
            if (gap[i] && ink[i] && !protectedStroke[i]) candidates.add(i);
        }
        // Prefer removing cells supported by fewer original ink pixels.
        candidates.sort(java.util.Comparator.comparingInt((Integer i) -> support[i]).thenComparingInt(i -> i));
        for (int index : candidates) {
            if (!safeToClear(ink, columns, lines, index)) continue;
            ink[index] = false;
            int x = index % columns * block, y = index / columns * block;
            for (int yy = y; yy < Math.min(h, y + block); yy++) {
                for (int xx = x; xx < Math.min(w, x + block); xx++) result[yy * w + xx] = 0;
            }
        }
    }

    private static boolean sourceInk(int[] rows, int left, int width, int x, int y) {
        int bit = 31 - left - x;
        return x >= 0 && x < width && y >= 0 && y < 16 && bit >= 0 && bit < 32
                && ((rows[y] >>> bit) & 1) != 0;
    }

    private static boolean safeToClear(boolean[] ink, int columns, int lines, int index) {
        int x = index % columns, y = index / columns;
        // Keep raster boundaries, isolated dots, endpoints and thin bridges.
        if (x == 0 || y == 0 || x == columns - 1 || y == lines - 1) return false;
        boolean[] neighbors = new boolean[9];
        int count = 0, start = -1;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int local = (dy + 1) * 3 + dx + 1;
                if (ink[(y + dy) * columns + x + dx]) {
                    neighbors[local] = true;
                    count++;
                    start = local;
                }
            }
        }
        if (count < 3) return false;
        int[] queue = new int[8];
        int head = 0, tail = 0;
        queue[tail++] = start;
        neighbors[start] = false;
        while (head < tail) {
            int current = queue[head++];
            for (int next = 0; next < 9; next++) {
                if (neighbors[next] && Math.abs(current % 3 - next % 3) <= 1
                        && Math.abs(current / 3 - next / 3) <= 1) {
                    neighbors[next] = false;
                    queue[tail++] = next;
                }
            }
        }
        return tail == count;
    }
}
