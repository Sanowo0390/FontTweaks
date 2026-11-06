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

    public static FontDescription select(FontDescription requested, boolean matches) {
        return matches && FontDescription.DEFAULT.equals(requested) ? MISAKI : requested;
    }

    public static FontDescription select(FontDescription requested, int codepoint,
                                         FontTweaksSettings cfg, int guiScale, boolean forceUnicode) {
        if (!FontDescription.DEFAULT.equals(requested) || !cfg.shouldReplace(codepoint)) return requested;
        if (cfg.resourcePackEnabled) return PIXELMPLUS;
        return select(requested, matchesEnglishGrid(cfg.pixelationEnabled,
                cfg.pixelBlockSize, guiScale, forceUnicode));
    }
}
