package dev.sanowo.fonttweaks;

import net.minecraft.client.Minecraft;

public final class FontTweaksReload {
    private static final ResourceReloadQueue QUEUE = new ResourceReloadQueue();
    private FontTweaksReload() {}
    public static void request() { QUEUE.request(System.nanoTime(), 0); }
    public static void requestAfterTyping() { QUEUE.request(System.nanoTime(), 350_000_000L); }
    public static void flush() { QUEUE.flush(); }
    public static void tick(Minecraft client) {
        QUEUE.tick(System.nanoTime(), client::reloadResourcePacks,
                () -> client.execute(() -> client.gui.hud.getChat().rescaleChat()),
                error -> System.err.println("[FontTweaks] Resource reload failed: " + error));
    }
}
