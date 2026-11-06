package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.network.chat.Component;

/** Uses actual multiline/tooltip paths instead of reproducing line spacing by hand. */
public final class FontTweaksPreviewScreen extends Screen implements PreviewScaleSource {
    private final Screen parent;
    private double activeScale = 1;
    private boolean spacingMode;
    public FontTweaksPreviewScreen(Screen parent, boolean spacingMode) {
        super(Component.translatable("fonttweaks.preview"));
        this.parent = parent;
        this.spacingMode = spacingMode;
    }
    public double previewScale() { return activeScale; }
    @Override protected void init() {
        int w = Math.min(320, this.width - 24), x = (this.width - w) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.preview_sizes"), button -> {
            spacingMode = false;
        }).bounds(x, 34, (w - 8) / 2, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.preview_spacing"), button -> {
            spacingMode = true;
        }).bounds(x + (w + 8) / 2, 34, (w - 8) / 2, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 60, this.height - 28, 120, 20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        FontTweaksSettings cfg = FontTweaksSettings.get();
        if (spacingMode) {
            int gap = Math.max(42, Math.min(72, (this.height - 126) / 3));
            spacingSample(graphics, "fonttweaks.global_spacing", cfg.globalLineSpacing, 70, 0);
            spacingSample(graphics, "fonttweaks.spacing", cfg.wrappedSpacing(), 70 + gap, 1);
            spacingSample(graphics, "fonttweaks.tooltip_spacing", cfg.tooltipSpacing(), 70 + gap * 2, 2);
        } else {
            int gap = Math.max(28, Math.min(50, (this.height - 126) / 4));
            sample(graphics, "fonttweaks.screen", cfg.scaleFor(true), 70);
            sample(graphics, "fonttweaks.hud", cfg.scaleFor(false), 70 + gap);
            sample(graphics, "fonttweaks.button", cfg.buttonTextScale(), 70 + gap * 2);
            sample(graphics, "fonttweaks.chat", cfg.chatTextScale(), 70 + gap * 3);
        }
    }
    private void spacingSample(GuiGraphicsExtractor graphics, String key, double spacing, int y, int kind) {
        graphics.centeredText(this.font, Component.translatable(key).append(" " + Math.round(spacing * 100) + "%"),
                this.width / 2, y, 0xFFAAAAAA);
        activeScale = FontTweaksSettings.get().scaleFor(true);
        FontTweaksRenderContext.Kind previous = FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
        try {
            Component first = Component.literal("日本語 ABC 1"), second = Component.literal("日本語 ABC 2");
            int x = this.width / 2 - 70;
            if (kind == 0) {
                MultiLineLabel.create(this.font, this.width - 40, first, second)
                        .visitLines(TextAlignment.CENTER, this.width / 2, y + 13, 9, graphics.textRenderer());
            } else if (kind == 1) {
                graphics.textWithWordWrap(this.font, first.copy().append("\n").append(second),
                        x, y + 13, this.width - x - 20, 0xFFFFFFFF);
            } else {
                ClientTextTooltip a = new ClientTextTooltip(first.getVisualOrderText());
                ClientTextTooltip b = new ClientTextTooltip(second.getVisualOrderText());
                a.extractText(graphics, this.font, x, y + 13);
                b.extractText(graphics, this.font, x, y + 13 + a.getHeight(this.font));
            }
        } finally { FontTweaksRenderContext.pop(previous); }
    }
    private void sample(GuiGraphicsExtractor graphics, String key, double scale, int y) {
        graphics.centeredText(this.font, Component.translatable(key).append(" " + Math.round(scale * 100) + "%"),
                this.width / 2, y, 0xFFAAAAAA);
        activeScale = scale;
        FontTweaksRenderContext.Kind previous = FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
        try { graphics.centeredText(this.font, Component.literal("日本語 ABC 012345"), this.width / 2, y + 13, 0xFFFFFF55); }
        finally { FontTweaksRenderContext.pop(previous); }
    }
    @Override public void onClose() { this.minecraft.gui.setScreen(parent); }
}
