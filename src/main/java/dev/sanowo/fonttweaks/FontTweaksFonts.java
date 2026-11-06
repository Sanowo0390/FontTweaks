package dev.sanowo.fonttweaks;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class FontTweaksFonts {
    public static final FontDescription.Resource MISAKI = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("fonttweaks", "misaki"));

    private FontTweaksFonts() {}

    /** Standard bitmap English has one source dot per GUI unit. Text sizing scales both equally. */
    public static boolean matchesEnglishGrid(boolean enabled, int targetPixels, int guiScale,
                                              boolean forceUnicode) {
        return enabled && !forceUnicode && guiScale > 0 && targetPixels == guiScale;
    }

    public static FontDescription select(FontDescription requested, boolean matches) {
        return matches && FontDescription.DEFAULT.equals(requested) ? MISAKI : requested;
    }
}
