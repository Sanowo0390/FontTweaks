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
            case "auto" -> matchesEnglishGrid(cfg.pixelationEnabled, cfg.pixelBlockSize,
                    guiScale, forceUnicode) ? MISAKI : PIXELMPLUS;
            default -> PIXELMPLUS;
        };
    }

    public static FontDescription select(FontDescription requested, int codepoint,
                                         FontTweaksSettings cfg, int guiScale, boolean forceUnicode) {
        if (!cfg.resourcePackEnabled || !FontDescription.DEFAULT.equals(requested)
                || !cfg.shouldReplace(codepoint)) return requested;
        return activePackFont(cfg, guiScale, forceUnicode);
    }
}
