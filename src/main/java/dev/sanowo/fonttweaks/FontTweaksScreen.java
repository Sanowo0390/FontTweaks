package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FontTweaksScreen extends Screen {
    private final Screen parent;

    public FontTweaksScreen(Screen parent) {
        super(Component.translatable("fonttweaks.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(310, this.width - 24);
        int x = (this.width - w) / 2;
        int startY = this.height / 2 - 78;
        FontTweaksSettings cfg = FontTweaksSettings.get();

        this.addRenderableWidget(new SettingSlider(x, startY, w, "fonttweaks.global",
                0.5f, 2.0f, () -> cfg.globalScale, v -> cfg.globalScale = v));
        this.addRenderableWidget(new SettingSlider(x, startY + 25, w, "fonttweaks.screen",
                0.5f, 2.0f, () -> cfg.screenScale, v -> cfg.screenScale = v));
        this.addRenderableWidget(new SettingSlider(x, startY + 50, w, "fonttweaks.hud",
                0.5f, 2.0f, () -> cfg.hudScale, v -> cfg.hudScale = v));
        this.addRenderableWidget(new SettingSlider(x, startY + 75, w, "fonttweaks.spacing",
                0.75f, 2.0f, () -> cfg.wrappedLineSpacing, v -> cfg.wrappedLineSpacing = v));
        this.addRenderableWidget(new SettingSlider(x, startY + 100, w, "fonttweaks.tooltip_spacing",
                0.75f, 2.0f, () -> cfg.tooltipLineSpacing, v -> cfg.tooltipLineSpacing = v));

        int buttonWidth = (w - 8) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset"), b -> {
            FontTweaksSettings.reset();
            this.rebuildWidgets();
        }).bounds(x, startY + 137, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(x + buttonWidth + 8, startY + 137, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 102, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("fonttweaks.help"),
                this.width / 2, this.height / 2 + 83, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        FontTweaksSettings.save();
        this.minecraft.gui.setScreen(this.parent);
    }

    private static final class SettingSlider extends AbstractSliderButton {
        private final String titleKey;
        private final float min;
        private final float max;
        private final Supplier<Float> getter;
        private final Consumer<Float> setter;

        SettingSlider(int x, int y, int width, String titleKey, float min, float max,
                      Supplier<Float> getter, Consumer<Float> setter) {
            super(x, y, width, 20, Component.empty(), (getter.get() - min) / (max - min));
            this.titleKey = titleKey;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            float amount = min + (float) this.value * (max - min);
            this.setMessage(Component.translatable(titleKey).append(" ")
                    .append(String.format(Locale.ROOT, "%.0f%%", amount * 100)));
        }

        @Override
        protected void applyValue() {
            float raw = min + (float) this.value * (max - min);
            float snapped = Math.round(raw * 20.0f) / 20.0f;
            setter.accept(FontTweaksSettings.bounded(snapped, min, max));
            FontTweaksSettings.save();
        }
    }
}
