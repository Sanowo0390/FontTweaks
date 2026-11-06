package dev.sanowo.fonttweaks;

/** One independent Minecraft GUI-scale profile; scale 4 covers GUI4 and higher. */
public final class GuiScaleProfile {
    /** Explicit pixel density: 1, 2, 3 or 4. */
    public int pixelBlockSize = 1;
    /** Prefer a bundled bitmap font rather than the vanilla Unifont. */
    public boolean useExistingFont = false;
    /** Migration-only preference: preserves an old explicit font selection. */
    public String legacyExistingFont;

    /** Per-scale Unifont processing; null means inherit pre-0.12 global setting. */
    public Boolean unifontProcessing;

    /** Per-scale English font replacement; null inherits pre-0.12 global setting. */
    public Boolean replaceEnglish;

    public GuiScaleProfile() {}

    public GuiScaleProfile(int pixelBlockSize, boolean useExistingFont, String legacyExistingFont) {
        this.pixelBlockSize = pixelBlockSize;
        this.useExistingFont = useExistingFont;
        this.legacyExistingFont = legacyExistingFont;
    }

    public int effectivePixels() {
        return pixelBlockSize;
    }
}
