package dev.sanowo.fonttweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Keep only FontTweaks' own configuration controls at their native size and spacing. */
public final class FontTweaksUiContext {
    private FontTweaksUiContext() {}

    public static boolean isOwnYaclScreen(Screen screen) {
        return screen != null
                && screen.getClass().getName().startsWith("dev.isxander.yacl3.")
                && screen.getTitle().getString().startsWith("FontTweaks ");
    }

    public static boolean isOwnSettingsScreen() {
        Minecraft client = Minecraft.getInstance();
        return client != null && client.gui != null && isOwnYaclScreen(client.gui.screen());
    }
}
