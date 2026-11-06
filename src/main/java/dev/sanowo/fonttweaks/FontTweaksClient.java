package dev.sanowo.fonttweaks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class FontTweaksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FontTweaksSettings.load();
        int[] previousGuiScale = {-1};
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            int scale = client.getWindow().getGuiScale();
            if (previousGuiScale[0] > 0 && previousGuiScale[0] != scale
                    && FontTweaksSettings.get().pixelationEnabled) FontTweaksReload.request();
            previousGuiScale[0] = scale;
            FontTweaksReload.tick(client);
        });
    }
}
