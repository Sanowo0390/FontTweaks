package dev.sanowo.fonttweaks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PixelGridMathTest {
    @Test
    void twoScreenPixelsPerFontDotAtGuiScaleThree() {
        double extra = PixelGridMath.multiplier(2, 3, 1.0);
        assertEquals(2.0 / 3.0, extra, 1e-12);
        assertEquals(2.0, extra * 3, 1e-12);
    }

    @Test
    void compensatesForParentPoseScaling() {
        for (double base : new double[] {0.5, 2.0, 1.3, 2.0 / 3.0}) {
            double extra = PixelGridMath.multiplier(2, 3, base);
            assertEquals(2.0, extra * base * 3, 1e-10);
        }
    }

    @Test
    void cyclesThroughOneTwoAndThreePixelsPerDot() {
        for (int pixels = 1; pixels <= 3; pixels++) {
            double additionalScale = PixelGridMath.multiplier(pixels, 3, 1.0);
            assertEquals(pixels, additionalScale * 3, 1e-12);
        }
    }

    @Test
    void snapsToPhysicalPixels() {
        float original = 12.23456f;
        float delta = PixelGridMath.snapDelta(original, 3);
        float physicalCoordinate = (original + delta) * 3;
        assertEquals(Math.round(original * 3), physicalCoordinate, 0.0001);
    }

    @Test
    void rejectsDegenerateMatricesAndInvalidScales() {
        assertEquals(1.0, PixelGridMath.multiplier(2, 3, 0.0));
        assertEquals(1.0, PixelGridMath.multiplier(2, 3, Double.NaN));
        assertEquals(1.0, PixelGridMath.multiplier(2, 0, 1));
        assertEquals(0.0f, PixelGridMath.snapDelta(Float.NaN, 3));
        assertTrue(Double.isFinite(PixelGridMath.multiplier(2, 3, 0.333)));
    }
}
