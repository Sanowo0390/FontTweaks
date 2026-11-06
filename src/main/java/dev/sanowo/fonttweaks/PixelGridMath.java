package dev.sanowo.fonttweaks;

/**
 * Math used to put each native 8x8 bitmap-font texel on an integer number
 * of physical display pixels. Kept independent of Minecraft for unit tests.
 */
public final class PixelGridMath {
    private PixelGridMath() {}

    /**
     * @param pixelsPerDot requested display pixels per source bitmap pixel
     * @param guiScale Minecraft's integer GUI scale (3 in the target mode)
     * @param parentScale length of the input text matrix's X basis vector
     * @return additional text matrix multiplier. Combined visible physical
     *         pixel width = parentScale * multiplier * guiScale.
     */
    public static double multiplier(int pixelsPerDot, int guiScale, double parentScale) {
        if (pixelsPerDot < 1 || guiScale < 1
                || !Double.isFinite(parentScale) || parentScale <= 0.00001) {
            return 1.0;
        }
        return (double) pixelsPerDot / ((double) guiScale * parentScale);
    }

    /** Returns the GUI-coordinate delta needed to snap to a physical pixel. */
    public static float snapDelta(float guiCoordinate, int guiScale) {
        if (guiScale < 1 || !Float.isFinite(guiCoordinate)) return 0.0f;
        return Math.round((double) guiCoordinate * guiScale) / (float) guiScale
                - guiCoordinate;
    }
}
