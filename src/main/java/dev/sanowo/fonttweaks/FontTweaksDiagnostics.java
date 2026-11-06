package dev.sanowo.fonttweaks;

/** Runtime proof that the 26.3 GUI text render-state hook was reached. */
public final class FontTweaksDiagnostics {
    private static volatile long previewSamples;
    private static volatile int lastGuiScale;
    private static volatile boolean lastMode;
    private static volatile int lastTargetPixels;
    private static volatile double lastEffectiveTextScale = 1.0;

    private FontTweaksDiagnostics() {}

    public static void recordPreview(int guiScale, boolean enabled, int pixelsPerDot, double effectiveTextScale) {
        lastGuiScale = guiScale;
        lastMode = enabled;
        lastTargetPixels = pixelsPerDot;
        lastEffectiveTextScale = effectiveTextScale;
        ++previewSamples;
    }

    public static String debugStatus() {
        if (previewSamples == 0) return "FontTweaks hook: NOT DETECTED";
        return String.format(java.util.Locale.ROOT,
                "Hook OK | GUI %d | Font %s | Target %dpx | Applied %.4fx",
                lastGuiScale, lastMode ? "Misaki" : "Default", lastTargetPixels, lastEffectiveTextScale);
    }
}
