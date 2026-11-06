package dev.sanowo.fonttweaks;

import net.minecraft.client.Minecraft;

public final class FontTweaksReload {
    private static final ResourceReloadQueue QUEUE = new ResourceReloadQueue();
    private FontTweaksReload() {}
    public static void request() { QUEUE.request(); }
    public static void tick(Minecraft client) {
        QUEUE.tick(client::reloadResourcePacks,
                () -> client.execute(() -> client.gui.hud.getChat().rescaleChat()),
                error -> System.err.println("[FontTweaks] Resource reload failed: " + error));
    }
}
