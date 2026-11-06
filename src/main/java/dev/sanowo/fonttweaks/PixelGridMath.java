package dev.sanowo.fonttweaks;

/**
 * Math used to put each native 8x8 bitmap-font texel on an integer number
 * of physical display pixels. Kept independent of Minecraft for unit tests.
 */
public final class PixelGridMath {
    private PixelGridMath() {}

    /** Returns the GUI-coordinate delta needed to snap to a physical pixel. */
    public static float snapDelta(float guiCoordinate, int guiScale) {
        if (guiScale < 1 || !Float.isFinite(guiCoordinate)) return 0.0f;
        return Math.round((double) guiCoordinate * guiScale) / (float) guiScale
                - guiCoordinate;
    }
}
