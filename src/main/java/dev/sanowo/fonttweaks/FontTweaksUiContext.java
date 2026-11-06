package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.screens.Screen;

/** Keep only FontTweaks' own YACL configuration unscaled.
 * Sodium, YACL and other mods' YACL screens remain eligible for text scaling.
 */
public final class FontTweaksUiContext {
    private FontTweaksUiContext() {}

    public static boolean isOwnYaclScreen(Screen screen) {
        return screen != null
                && screen.getClass().getName().startsWith("dev.isxander.yacl3.")
                && screen.getTitle().getString().startsWith("FontTweaks ");
    }
}
