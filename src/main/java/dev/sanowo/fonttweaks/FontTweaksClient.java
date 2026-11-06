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
            // Raster resolution is always GUI3 referenced, but native versus
            // processed glyph variants cannot share a cached baked glyph.
            // Refresh only when that actual representation changes.
            if (previousGuiScale[0] > 0 && previousGuiScale[0] != scale) {
                if (client.gui != null) client.gui.hud.getChat().rescaleChat();
                // Font glyphs are cached. A reload is necessary ONLY when
                // crossing native/processed atlas boundaries, not for every
                // GUI scale change or the GUI2/4 native 1px transition.
                FontTweaksSettings cfg = FontTweaksSettings.get();
                if (cfg.pixelRasterSignature(previousGuiScale[0])
                        != cfg.pixelRasterSignature(scale)) {
                    FontTweaksReload.request();
                }
            }
            previousGuiScale[0] = scale;
            FontTweaksReload.tick(client);
        });
    }
}
