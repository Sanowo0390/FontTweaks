package dev.sanowo.fonttweaks;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/** Select before FontManager's cache, for both width measurement and drawing. */
public final class FontTweaksFonts {
    public static final FontDescription.Resource MISAKI = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("fonttweaks", "misaki"));

    private FontTweaksFonts() {}

    public static FontDescription select(FontDescription requested, boolean enabled) {
        return enabled && FontDescription.DEFAULT.equals(requested) ? MISAKI : requested;
    }
}
