package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FontTweaksFontScreen extends Screen {
    private final Screen parent;
    public FontTweaksFontScreen(Screen parent) {
        super(Component.translatable("fonttweaks.font_settings"));
        this.parent = parent;
    }
    @Override protected void init() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        int w = Math.min(360, this.width - 24), x = (this.width - w) / 2;
        option(x, 45, w, Component.translatable(cfg.resourcePackEnabled
                ? "fonttweaks.pack_on" : "fonttweaks.pack_off"),
                () -> cfg.resourcePackEnabled = !cfg.resourcePackEnabled,
                () -> cfg.resourcePackEnabled = false);
        option(x, 69, w, Component.translatable("fonttweaks.pack_font_" + cfg.resourcePackFont),
                cfg::cycleResourcePackFont, () -> cfg.resourcePackFont = "pixelmplus12")
                .active = cfg.resourcePackEnabled;
        option(x, 93, w, Component.translatable(cfg.replaceEnglish
                ? "fonttweaks.english_on" : "fonttweaks.english_off"),
                () -> cfg.replaceEnglish = !cfg.replaceEnglish, () -> cfg.replaceEnglish = false);
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.blacklist_count", cfg.blacklistedEntries.size()),
                button -> this.minecraft.gui.setScreen(new FontTweaksBlacklistScreen(this)))
                .bounds(x, 117, w - 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset_short"), button -> {
            cfg.clearBlacklist(); changed();
        }).bounds(x + w - 40, 117, 40, 20).build());
        int half = (w - 8) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset_all"), button -> {
            FontTweaksSettings.reset(); changed();
        }).bounds(x, this.height - 30, half, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x + half + 8, this.height - 30, half, 20).build());
    }
    private Button option(int x, int y, int width, Component title, Runnable change, Runnable reset) {
        Button main = this.addRenderableWidget(Button.builder(title, button -> { change.run(); changed(); })
                .bounds(x, y, width - 44, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset_short"), button -> {
            reset.run(); changed();
        }).bounds(x + width - 40, y, 40, 20).build());
        return main;
    }
    private void changed() {
        FontTweaksSettings.save(); FontTweaksReload.request(); this.rebuildWidgets();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int w = Math.min(360, this.width - 24), x = (this.width - w) / 2;
        graphics.centeredText(this.font, this.title, this.width / 2, 22, 0xFFFFFFFF);
        graphics.textWithWordWrap(this.font, Component.translatable("fonttweaks.blacklist_help"), x, 150, w, 0xFFAAAAAA);
    }
    @Override public void onClose() {
        FontTweaksSettings.save(); this.minecraft.gui.setScreen(parent); FontTweaksReload.flush();
    }
}
