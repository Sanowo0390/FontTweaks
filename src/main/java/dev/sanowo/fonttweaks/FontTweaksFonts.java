package dev.sanowo.fonttweaks;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class FontTweaksFonts {
    public static final FontDescription.Resource MISAKI = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("fonttweaks", "misaki"));

    private FontTweaksFonts() {}
    public static final FontDescription.Resource PIXELMPLUS = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("fonttweaks", "pixelmplus12"));

    /** Standard bitmap English has one source dot per GUI unit. Text sizing scales both equally. */
    public static boolean matchesEnglishGrid(boolean enabled, int targetPixels, int guiScale,
                                              boolean forceUnicode) {
        return enabled && !forceUnicode && guiScale > 0 && targetPixels == guiScale;
    }

    /** Native Unihex uses two source pixels per GUI unit. */
    public static boolean matchesNativeUnifontGrid(int targetPixels, int guiScale) {
        return guiScale > 0 && targetPixels > 0 && targetPixels * 2 == guiScale;
    }

    public static FontDescription activePackFont(FontTweaksSettings cfg, int guiScale, boolean forceUnicode) {
        if (!cfg.resourcePackEnabled) return null;
        return switch (cfg.resourcePackFont) {
            case "misaki" -> MISAKI;
            case "auto" -> matchesEnglishGrid(cfg.pixelationEnabled, cfg.effectivePixelBlockSize(),
                    guiScale, forceUnicode) ? MISAKI : PIXELMPLUS;
            default -> PIXELMPLUS;
        };
    }

    /** Dynamic font fallback: GUI 3+ keeps the normal configuration, GUI 1/2 uses Misaki. */
    public static boolean useCompactGuiFallback(FontTweaksSettings cfg, int guiScale) {
        return cfg.guiScaleDependentFont && guiScale > 0 && guiScale < 3;
    }

    public static FontDescription select(FontDescription requested, int codepoint,
                                         FontTweaksSettings cfg, int guiScale, boolean forceUnicode) {
        // Respect explicitly selected fonts and per-codepoint exclusions.
        if (!FontDescription.DEFAULT.equals(requested) || !cfg.shouldReplace(codepoint)) {
            return requested;
        }
        if (cfg.hasGuiProfiles()) {
            GuiScaleProfile profile = cfg.profileFor(guiScale);
            if (!profile.useExistingFont) return requested;
            // Explicit rule requested by user: 3px + existing font ON
            // selects bundled Misaki for EVERY GUI scale, including 1 and 5+.
            if (profile.effectivePixels() == 3
                    || "misaki".equals(profile.legacyExistingFont)) return MISAKI;
            return PIXELMPLUS;
        }
        // This works even if normal font replacement is off: GUI 3 can use
        // processed vanilla Unifont, while GUI 2 immediately uses crisp Misaki.
        // No font atlas rebuild or resource reload is necessary.
        if (cfg.guiScaleDependentFont) {
            // The unified "Auto" choice always means native at GUI 3+,
            // Misaki at GUI 1/2, even for old configs with both flags set.
            return useCompactGuiFallback(cfg, guiScale) ? MISAKI : requested;
        }
        if (!cfg.resourcePackEnabled) return requested;
        return activePackFont(cfg, guiScale, forceUnicode);
    }
}
