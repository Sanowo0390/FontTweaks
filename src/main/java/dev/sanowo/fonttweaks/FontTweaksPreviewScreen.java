package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Real text states using the currently selected fonts, with fixed-size controls. */
public final class FontTweaksPreviewScreen extends Screen implements PreviewScaleSource {
    private final Screen parent;
    private double activeScale = 1;
    public FontTweaksPreviewScreen(Screen parent) {
        super(Component.translatable("fonttweaks.preview"));
        this.parent = parent;
    }
    public double previewScale() { return activeScale; }
    @Override protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 60, this.height - 28, 120, 20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        FontTweaksSettings cfg = FontTweaksSettings.get();
        int gap = Math.max(28, Math.min(42, (this.height - 100) / 5));
        sample(graphics, "fonttweaks.screen", cfg.scaleFor(true), 36);
        sample(graphics, "fonttweaks.hud", cfg.scaleFor(false), 36 + gap);
        sample(graphics, "fonttweaks.button", cfg.buttonTextScale(), 36 + gap * 2);
        sample(graphics, "fonttweaks.chat", cfg.chatTextScale(), 36 + gap * 3);
        int y = 36 + gap * 4;
        graphics.centeredText(this.font, Component.translatable("fonttweaks.global_spacing"),
                this.width / 2, y, 0xFFAAAAAA);
        drawSample(graphics, Component.literal("日本語 ABC 012345"), cfg.scaleFor(true), y + 13);
        drawSample(graphics, Component.literal("次の行 / Next line"), cfg.scaleFor(true),
                y + 13 + Math.max(1, (int) Math.round(cfg.lineStep(9) * cfg.scaleFor(true))));
    }
    private void sample(GuiGraphicsExtractor graphics, String key, double scale, int y) {
        graphics.centeredText(this.font, Component.translatable(key).append(" " + Math.round(scale * 100) + "%"),
                this.width / 2, y, 0xFFAAAAAA);
        drawSample(graphics, Component.literal("日本語 ABC 012345"), scale, y + 13);
    }
    private void drawSample(GuiGraphicsExtractor graphics, Component text, double scale, int y) {
        activeScale = scale;
        FontTweaksRenderContext.Kind previous = FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
        try { graphics.centeredText(this.font, text, this.width / 2, y, 0xFFFFFF55); }
        finally { FontTweaksRenderContext.pop(previous); }
    }
    @Override public void onClose() { this.minecraft.gui.setScreen(parent); }
}
