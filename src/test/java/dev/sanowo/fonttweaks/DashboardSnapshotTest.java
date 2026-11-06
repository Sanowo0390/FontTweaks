package dev.sanowo.fonttweaks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The dashboard's Undo button must restore an independent copy. */
class DashboardSnapshotTest {
    @Test
    void snapshotIsDeepAndPreservesPreciseValuesAndBlacklists() {
        FontTweaksSettings source = FontTweaksSettings.get();
        FontTweaksSettings first = FontTweaksSettings.snapshot();
        FontTweaksSettings second = FontTweaksSettings.snapshot();

        assertNotSame(source, first);
        assertNotSame(source.blacklistedEntries, first.blacklistedEntries);
        assertNotSame(first, second);
        assertEquals(source.globalScale, first.globalScale);
        assertEquals(source.pixelBlockSize, first.pixelBlockSize);
        assertEquals(source.resourcePackFont, first.resourcePackFont);

        first.globalScale = 0.6666666666666667;
        first.blacklistedEntries.add("試");
        assertEquals(0.6666666666666667, first.globalScale, 0.0);
        assertNotEquals(first.globalScale, second.globalScale);
        assertFalse(second.blacklistedEntries.contains("試"));
    }
}
