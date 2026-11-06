package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Dedicated font page keeps the main size controls usable at GUI 3/4. */
public final class FontTweaksFontScreen extends Screen {
    private final Screen parent;
    private boolean changed;

    public FontTweaksFontScreen(Screen parent) {
        super(Component.translatable("fonttweaks.font_settings"));
        this.parent = parent;
    }

    @Override protected void init() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        int width = Math.min(360, this.width - 24);
        int x = (this.width - width) / 2;
        this.addRenderableWidget(Button.builder(packLabel(), button -> {
            cfg.resourcePackEnabled = !cfg.resourcePackEnabled;
            button.setMessage(packLabel());
            changed = true;
            FontTweaksSettings.save();
        }).bounds(x, 45, width, 20).build());
        this.addRenderableWidget(Button.builder(englishLabel(), button -> {
            cfg.replaceEnglish = !cfg.replaceEnglish;
            button.setMessage(englishLabel());
            changed = true;
            FontTweaksSettings.save();
        }).bounds(x, 69, width, 20).build());
        EditBox blacklist = new EditBox(this.font, x, 115, width, 20,
                Component.translatable("fonttweaks.blacklist"));
        blacklist.setMaxLength(4096);
        blacklist.setValue(cfg.blacklistedCharacters);
        blacklist.setResponder(value -> {
            cfg.blacklistedCharacters = value;
            changed = true;
            FontTweaksSettings.save();
        });
        this.addRenderableWidget(blacklist);
        int half = (width - 8) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset"), button -> {
            FontTweaksSettings.reset();
            changed = true;
            this.rebuildWidgets();
        }).bounds(x, this.height - 30, half, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x + half + 8, this.height - 30, half, 20).build());
    }

    private Component packLabel() {
        return Component.translatable(FontTweaksSettings.get().resourcePackEnabled
                ? "fonttweaks.pack_on" : "fonttweaks.pack_off");
    }

    private Component englishLabel() {
        return Component.translatable(FontTweaksSettings.get().replaceEnglish
                ? "fonttweaks.english_on" : "fonttweaks.english_off");
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int width = Math.min(360, this.width - 24);
        int x = (this.width - width) / 2;
        graphics.centeredText(this.font, this.title, this.width / 2, 22, 0xFFFFFFFF);
        graphics.text(this.font, Component.translatable("fonttweaks.blacklist"), x, 101, 0xFFFFFFFF);
        graphics.textWithWordWrap(this.font, Component.translatable("fonttweaks.blacklist_help"),
                x, 144, width, 0xFFAAAAAA);
    }

    @Override public void onClose() {
        FontTweaksSettings.save();
        this.minecraft.gui.setScreen(parent);
        if (changed) {
            this.minecraft.reloadResourcePacks();
            this.minecraft.gui.hud.getChat().rescaleChat();
        }
    }
}
