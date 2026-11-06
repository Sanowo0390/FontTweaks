package dev.sanowo.fonttweaks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PixelGridMathTest {
    @Test
    void snapsToPhysicalPixels() {
        float original = 12.23456f;
        float delta = PixelGridMath.snapDelta(original, 3);
        float physicalCoordinate = (original + delta) * 3;
        assertEquals(Math.round(original * 3), physicalCoordinate, 0.0001);
    }

    @Test
    void rejectsInvalidCoordinatesAndScales() {
        assertEquals(0.0f, PixelGridMath.snapDelta(Float.NaN, 3));
        assertEquals(0.0f, PixelGridMath.snapDelta(2.5f, 0));
    }
}

