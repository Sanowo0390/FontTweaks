package dev.sanowo.fonttweaks;

import net.fabricmc.api.ClientModInitializer;

public final class FontTweaksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FontTweaksSettings.load();
    }
}
