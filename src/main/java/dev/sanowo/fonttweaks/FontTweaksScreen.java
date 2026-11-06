package dev.sanowo.fonttweaks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FontTweaksScreen extends Screen implements PreviewScaleSource {
    private final Screen parent;
    private String previewKey = "fonttweaks.screen";

    public double previewScale() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        return switch (previewKey) {
            case "fonttweaks.hud" -> cfg.scaleFor(false);
            case "fonttweaks.button" -> cfg.buttonTextScale();
            case "fonttweaks.chat" -> cfg.chatTextScale();
            default -> cfg.scaleFor(true);
        };
    }

    private final List<AbstractWidget> scaleControls = new ArrayList<>();
    private final List<AbstractWidget> spacingControls = new ArrayList<>();
    private Button pixelSizeButton;

    public FontTweaksScreen(Screen parent) {
        super(Component.translatable("fonttweaks.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        scaleControls.clear();
        spacingControls.clear();
        int w = Math.min(350, this.width - 24);
        int x = (this.width - w) / 2;
        int gap = this.height < 300 ? 20 : 24;
        int startY = Math.max(26, (this.height - (10 * gap + 44)) / 2);
        FontTweaksSettings cfg = FontTweaksSettings.get();

        addSettingRow(x, startY, w, "fonttweaks.global", 0.5, 2.0,
                () -> cfg.globalScale, v -> {
                    cfg.globalScale = v;
                    if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
                });
        addSettingRow(x, startY + gap, w, "fonttweaks.screen", 0.5, 2.0,
                () -> cfg.screenScale, v -> cfg.screenScale = v);
        addSettingRow(x, startY + gap * 2, w, "fonttweaks.hud", 0.5, 2.0,
                () -> cfg.hudScale, v -> cfg.hudScale = v);
        addSettingRow(x, startY + gap * 3, w, "fonttweaks.button", 0.5, 2.0,
                () -> cfg.buttonScale, v -> cfg.buttonScale = v);
        addSettingRow(x, startY + gap * 4, w, "fonttweaks.chat", 0.5, 2.0,
                () -> cfg.chatScale, v -> {
                    cfg.chatScale = v;
                    if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
                });
        addSettingRow(x, startY + gap * 5, w, "fonttweaks.global_spacing", 0.5, 2.0,
                () -> cfg.globalLineSpacing, v -> {
                    cfg.globalLineSpacing = v;
                    if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
                });
        addSettingRow(x, startY + gap * 6, w, "fonttweaks.spacing", 0.5, 2.0,
                () -> cfg.wrappedLineSpacing, v -> cfg.wrappedLineSpacing = v);
        addSettingRow(x, startY + gap * 7, w, "fonttweaks.tooltip_spacing", 0.5, 2.0,
                () -> cfg.tooltipLineSpacing, v -> cfg.tooltipLineSpacing = v);

        int half = (w - 8) / 2;
        this.addRenderableWidget(Button.builder(pixelToggleLabel(cfg), b -> {
            cfg.pixelationEnabled = !cfg.pixelationEnabled;
            b.setMessage(pixelToggleLabel(cfg));
            FontTweaksSettings.save();
            if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
            reloadPixelation();
            this.rebuildWidgets();
        }).bounds(x, startY + 8 * gap, half, 20).build());
        pixelSizeButton = this.addRenderableWidget(Button.builder(pixelSizeLabel(cfg), b -> {
            cfg.pixelBlockSize = cfg.nextPixelBlockSize();
            b.setMessage(pixelSizeLabel(cfg));
            FontTweaksSettings.save();
            if (this.minecraft != null) this.minecraft.gui.hud.getChat().rescaleChat();
            reloadPixelation();
        }).bounds(x + half + 8, startY + 8 * gap, half, 20).build());

        int third = (w - 16) / 3;
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.font_settings"), b -> {
            this.minecraft.gui.setScreen(new FontTweaksFontScreen(this));
        }).bounds(x, startY + 9 * gap, third, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fonttweaks.preview"), b -> {
            this.minecraft.gui.setScreen(new FontTweaksPreviewScreen(this));
        }).bounds(x + third + 8, startY + 9 * gap, third, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(x + (third + 8) * 2, startY + 9 * gap, third, 20).build());
        updateControlAvailability();
    }

    private void updateControlAvailability() {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        // Text sizing and spacing stay available with either effect state.
        for (AbstractWidget widget : scaleControls) widget.active = true;
        for (AbstractWidget widget : spacingControls) widget.active = true;
        pixelSizeButton.active = cfg.pixelationEnabled;
        pixelSizeButton.setTooltip(Tooltip.create(Component.translatable("fonttweaks.pixel_help")));
    }

    private void reloadPixelation() {
        // Uploaded glyphs are cached in atlases. Rebuild them for ON/OFF and block changes.
        FontTweaksReload.request();
    }

    private static Component pixelToggleLabel(FontTweaksSettings cfg) {
        return Component.translatable(cfg.pixelationEnabled
                ? "fonttweaks.pixel_mode_on" : "fonttweaks.pixel_mode_off");
    }

    private static Component pixelSizeLabel(FontTweaksSettings cfg) {
        return Component.translatable("fonttweaks.pixel_density", cfg.pixelBlockSize);
    }

    /**
     * A slider is convenient for rough adjustment, while the text field preserves
     * precise decimal values such as 0.6666666666666667 (2/3) as a double.
     */
    private void addSettingRow(int x, int y, int width, String key, double min, double max,
                               Supplier<Double> getter, Consumer<Double> setter) {
        Consumer<Double> apply = value -> {
            if (key.equals("fonttweaks.global") || key.equals("fonttweaks.screen")
                    || key.equals("fonttweaks.hud") || key.equals("fonttweaks.button")
                    || key.equals("fonttweaks.chat")) previewKey = key;
            setter.accept(value);
        };
        int inputWidth = Math.min(112, Math.max(86, width / 3));
        int sliderWidth = width - inputWidth - 8;

        EditBox input = new EditBox(this.font, x + sliderWidth + 8, y,
                inputWidth, 20, Component.translatable(key));
        input.setMaxLength(32);
        input.setValue(formatNumber(getter.get()));

        SettingSlider slider = new SettingSlider(x, y, sliderWidth, key, min, max,
                getter, value -> {
                    apply.accept(value);
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
                apply.accept(exact);
                slider.updateFromField(exact);
                FontTweaksSettings.save();
            } catch (NumberFormatException ignored) {
                input.setTextColor(0xFF5555);
            }
        });

        this.addRenderableWidget(slider);
        this.addRenderableWidget(input);
        List<AbstractWidget> controls = key.equals("fonttweaks.global_spacing") || key.equals("fonttweaks.spacing")
                || key.equals("fonttweaks.tooltip_spacing") ? spacingControls : scaleControls;
        controls.add(slider);
        controls.add(input);
    }

    private static String formatNumber(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int gap = this.height < 300 ? 20 : 24;
        int startY = Math.max(26, (this.height - (10 * gap + 44)) / 2);
        graphics.centeredText(this.font, this.title, this.width / 2, startY - 18, 0xFFFFFFFF);
        if (this.height >= 270) {
            graphics.centeredText(this.font, Component.translatable("fonttweaks.preview_current", Component.translatable(previewKey), Math.round(previewScale() * 100)),
                    this.width / 2, startY + 10 * gap + 2, 0xFFAAAAAA);

            // The sample uses the independent text size; bitmap processing is global.
            FontTweaksRenderContext.Kind prev =
                    FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
            try {
                graphics.centeredText(this.font, Component.literal("日本語 ABC 012345"),
                        this.width / 2, startY + 10 * gap + 19, 0xFFFFFF55);
            } finally {
                FontTweaksRenderContext.pop(prev);
            }

            boolean matched = FontTweaksFonts.matchesEnglishGrid(
                    FontTweaksSettings.get().pixelationEnabled, FontTweaksSettings.get().pixelBlockSize,
                    this.minecraft.getWindow().getGuiScale(), this.minecraft.options.forceUnicodeFont().get());
            int guiScale = this.minecraft.getWindow().getGuiScale();
            boolean nativeGrid = FontTweaksFonts.matchesNativeUnifontGrid(
                    FontTweaksSettings.get().pixelBlockSize, guiScale);
            String statusKey = !FontTweaksSettings.get().pixelationEnabled ? "fonttweaks.status_off"
                    : matched ? "fonttweaks.status_misaki"
                    : nativeGrid ? "fonttweaks.status_native" : "fonttweaks.status_unifont";
            if (FontTweaksSettings.get().resourcePackEnabled) statusKey = "fonttweaks.status_pixelmplus";
            graphics.centeredText(this.font, Component.translatable(statusKey,
                    guiScale, FontTweaksSettings.get().pixelBlockSize),
                    this.width / 2, startY + 10 * gap + 34, 0xFF88FFFF);
        }
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
