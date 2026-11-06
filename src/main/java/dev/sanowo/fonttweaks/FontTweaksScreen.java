package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
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
        int w = Math.min(340, this.width - 24);
        int x = (this.width - w) / 2;
        int startY = this.height / 2 - 78;
        FontTweaksSettings cfg = FontTweaksSettings.get();

        addSettingRow(x, startY, w, "fonttweaks.global", 0.5, 2.0,
                () -> cfg.globalScale, v -> cfg.globalScale = v);
        addSettingRow(x, startY + 25, w, "fonttweaks.screen", 0.5, 2.0,
                () -> cfg.screenScale, v -> cfg.screenScale = v);
        addSettingRow(x, startY + 50, w, "fonttweaks.hud", 0.5, 2.0,
                () -> cfg.hudScale, v -> cfg.hudScale = v);
        addSettingRow(x, startY + 75, w, "fonttweaks.spacing", 0.5, 2.0,
                () -> cfg.wrappedLineSpacing, v -> cfg.wrappedLineSpacing = v);
        addSettingRow(x, startY + 100, w, "fonttweaks.tooltip_spacing", 0.5, 2.0,
                () -> cfg.tooltipLineSpacing, v -> cfg.tooltipLineSpacing = v);

        int buttonWidth = (w - 8) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.reset"), b -> {
            FontTweaksSettings.reset();
            this.rebuildWidgets();
        }).bounds(x, startY + 137, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(x + buttonWidth + 8, startY + 137, buttonWidth, 20).build());
    }

    /**
     * A slider is convenient for rough adjustment, while the text field preserves
     * precise decimal values such as 0.6666666666666667 (2/3) as a double.
     */
    private void addSettingRow(int x, int y, int width, String key, double min, double max,
                               Supplier<Double> getter, Consumer<Double> setter) {
        int inputWidth = Math.min(112, Math.max(86, width / 3));
        int sliderWidth = width - inputWidth - 8;

        EditBox input = new EditBox(this.font, x + sliderWidth + 8, y,
                inputWidth, 20, Component.translatable(key));
        input.setMaxLength(32);
        input.setValue(formatNumber(getter.get()));

        SettingSlider slider = new SettingSlider(x, y, sliderWidth, key, min, max,
                getter, value -> {
                    setter.accept(value);
                    input.setValue(formatNumber(value));
                    FontTweaksSettings.save();
                });

        input.setResponder(raw -> {
            try {
                // Do not overwrite a partially typed number. Invalid values are
                // highlighted until the user finishes typing a valid number.
                double exact = Double.parseDouble(raw.trim());
                if (!Double.isFinite(exact) || exact < min || exact > max) {
                    input.setTextColor(0xFF5555);
                    return;
                }
                input.setTextColor(0xFFFFFF);
                setter.accept(exact);
                slider.updateFromField(exact);
                FontTweaksSettings.save();
            } catch (NumberFormatException ignored) {
                input.setTextColor(0xFF5555);
            }
        });

        this.addRenderableWidget(slider);
        this.addRenderableWidget(input);
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
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
        private final double min;
        private final double max;
        private final Supplier<Double> getter;
        private final Consumer<Double> setter;

        SettingSlider(int x, int y, int width, String titleKey, double min, double max,
                      Supplier<Double> getter, Consumer<Double> setter) {
            super(x, y, width, 20, Component.empty(), (getter.get() - min) / (max - min));
            this.titleKey = titleKey;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
            updateMessage();
        }

        void updateFromField(double exact) {
            this.value = (exact - min) / (max - min);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(titleKey).append(" ")
                    .append(String.format(Locale.ROOT, "%.2f%%", getter.get() * 100.0)));
        }

        @Override
        protected void applyValue() {
            double raw = min + this.value * (max - min);
            double snapped = Math.round(raw * 1000.0) / 1000.0;
            setter.accept(FontTweaksSettings.bounded(snapped, min, max));
            updateMessage();
        }
    }
}
