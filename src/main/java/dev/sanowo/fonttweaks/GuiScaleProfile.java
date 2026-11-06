package dev.sanowo.fonttweaks;

/** One independent Minecraft GUI-scale profile; scale 5 covers 5+. */
public final class GuiScaleProfile {
    /** 0=Auto (GUI3 reference 2px), 1..4=manual screen-pixel density. */
    public int pixelBlockSize = 1;
    /** Prefer a bundled bitmap font rather than the vanilla Unifont. */
    public boolean useExistingFont = false;
    /** Migration-only preference: preserves an old explicit font selection. */
    public String legacyExistingFont;

    public GuiScaleProfile() {}

    public GuiScaleProfile(int pixelBlockSize, boolean useExistingFont, String legacyExistingFont) {
        this.pixelBlockSize = pixelBlockSize;
        this.useExistingFont = useExistingFont;
        this.legacyExistingFont = legacyExistingFont;
    }

    public int effectivePixels() {
        return pixelBlockSize == FontTweaksSettings.AUTO_PIXEL_SIZE
                ? FontTweaksSettings.AUTO_REFERENCE_PIXEL_SIZE : pixelBlockSize;
    }
}
