package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** One row per Unicode codepoint, with explicit add/delete actions. */
public final class FontTweaksBlacklistScreen extends Screen {
    private final Screen parent;
    private int page;
    private String draft = "";
    public FontTweaksBlacklistScreen(Screen parent) {
        super(Component.translatable("fonttweaks.blacklist"));
        this.parent = parent;
    }
    @Override protected void init() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        int w = Math.min(360, this.width - 24), x = (this.width - w) / 2;
        int rows = Math.max(1, (this.height - 146) / 24);
        int pages = Math.max(1, (cfg.blacklistedEntries.size() + rows - 1) / rows);
        page = Math.min(page, pages - 1);
        EditBox input = new EditBox(this.font, x, 42, w - 64, 20, Component.translatable("fonttweaks.blacklist_add"));
        input.setTextColor(0xFFFFFFFF);
        input.setTextColorUneditable(0xFFFFFFFF);
        input.setMaxLength(4096);
        input.setValue(draft);
        input.setResponder(value -> draft = value);
        this.addRenderableWidget(input);
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.blacklist_add"), button -> {
            if (cfg.addBlacklistCharacters(draft)) {
                FontTweaksSettings.save();
                if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
            }
            draft = "";
            page = Math.max(0, (cfg.blacklistedEntries.size() - 1) / rows);
            this.rebuildWidgets();
        }).bounds(x + w - 60, 42, 60, 20).build());
        for (int row = 0; row < rows; row++) {
            int index = page * rows + row;
            if (index >= cfg.blacklistedEntries.size()) break;
            String entry = cfg.blacklistedEntries.get(index);
            int cp = entry.codePointAt(0), y = 84 + row * 24;
            EditBox display = new EditBox(this.font, x, y, w - 64, 20, Component.literal(entry));
            display.setValue(entry + String.format(java.util.Locale.ROOT, "   U+%04X", cp));
            display.setEditable(false);
            display.setTextColorUneditable(0xFFFFFFFF);
            this.addRenderableWidget(display);
            this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.blacklist_delete"), button -> {
                cfg.blacklistedEntries.remove(index);
                FontTweaksSettings.save();
                if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
                this.rebuildWidgets();
            }).bounds(x + w - 60, y, 60, 20).build());
        }
        Button previous = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            page--; this.rebuildWidgets();
        }).bounds(x, this.height - 58, 40, 20).build());
        previous.active = page > 0;
        Button next = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            page++; this.rebuildWidgets();
        }).bounds(x + w - 40, this.height - 58, 40, 20).build());
        next.active = page < pages - 1;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 60, this.height - 30, 120, 20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 18, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("fonttweaks.blacklist_list_help"),
                this.width / 2, 68, 0xFFAAAAAA);
        int rows = Math.max(1, (this.height - 146) / 24);
        int pages = Math.max(1, (FontTweaksSettings.get().blacklistedEntries.size() + rows - 1) / rows);
        graphics.centeredText(this.font, Component.literal((page + 1) + " / " + pages),
                this.width / 2, this.height - 52, 0xFFFFFFFF);
    }
    @Override public void onClose() {
        FontTweaksSettings.save();
        this.minecraft.gui.setScreen(parent);
    }
}
