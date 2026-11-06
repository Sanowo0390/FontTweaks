package dev.sanowo.fonttweaks;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Built with the same YACL v3 GUI API used by Zoomify 2.17.0.
 *
 * Source code and UI widget implementations are not copied from Zoomify:
 * the mod depends on the separately distributed YACL library. YACL provides
 * the tabs, scrolling category list, reset controls and description pane.
 */
public final class FontTweaksYaclScreen {
    private FontTweaksYaclScreen() {}

    private enum FontChoice implements NameableEnum {
        PIXELMPLUS("pixelmplus12", "fonttweaks.pack_font_pixelmplus12"),
        MISAKI("misaki", "fonttweaks.pack_font_misaki"),
        AUTO("auto", "fonttweaks.pack_font_auto");

        private final String id;
        private final String translationKey;
        FontChoice(String id, String key) { this.id = id; this.translationKey = key; }

        @Override
        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }

        static FontChoice fromId(String id) {
            for (FontChoice choice : values()) if (choice.id.equals(id)) return choice;
            return PIXELMPLUS;
        }
    }

    /** YACL enum controller with an explicit Auto choice (config value 0). */
    private enum PixelSizeChoice implements NameableEnum {
        AUTO(0, "fonttweaks.ui.dot.auto"),
        ONE(1, "fonttweaks.ui.dot.1"),
        TWO(2, "fonttweaks.ui.dot.2"),
        THREE(3, "fonttweaks.ui.dot.3"),
        FOUR(4, "fonttweaks.ui.dot.4");

        private final int pixels;
        private final String label;
        PixelSizeChoice(int pixels, String label) {
            this.pixels = pixels;
            this.label = label;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(label);
        }

        static PixelSizeChoice fromPixels(int pixels) {
            for (PixelSizeChoice choice : values()) {
                if (choice.pixels == pixels) return choice;
            }
            return ONE;
        }
    }

    private static OptionDescription description(String key) {
        return OptionDescription.of(Component.translatable(key));
    }

    private static OptionDescription illustrated(String key) {
        return OptionDescription.createBuilder()
                .text(Component.translatable(key))
                .customImage(new FontTweaksFontImage())
                .build();
    }

    private static Option<Double> number(String key, String help, Supplier<Double> getter,
                                         Consumer<Double> setter) {
        return Option.<Double>createBuilder()
                .name(Component.translatable(key))
                .description(description(help))
                .binding(1.0, getter, setter)
                // A field, rather than a stepped slider, is intentional:
                // typing 0.6666666666666667 must not lose precision.
                .controller(option -> DoubleFieldControllerBuilder.create(option).range(0.5, 2.0))
                .build();
    }

    private static Option<Boolean> toggle(String key, String help, boolean defaultValue,
                                           Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(key))
                .description(description(help))
                .binding(defaultValue, getter, setter)
                .controller(BooleanControllerBuilder::create)
                .build();
    }

    public static Screen create(Screen parent) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        final boolean initialPixels = cfg.pixelationEnabled;
        final int initialRasterPixels = cfg.effectivePixelBlockSize();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("fonttweaks.ui.title"))
                // Text tab: font sizes and letter/line spacing share one category.
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.text"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("fonttweaks.ui.group.sizes"))
                                .option(number("fonttweaks.global", "fonttweaks.ui.desc.global",
                                        () -> cfg.globalScale, value -> cfg.globalScale = value))
                                .option(number("fonttweaks.screen", "fonttweaks.ui.desc.screen",
                                        () -> cfg.screenScale, value -> cfg.screenScale = value))
                                .option(number("fonttweaks.hud", "fonttweaks.ui.desc.hud",
                                        () -> cfg.hudScale, value -> cfg.hudScale = value))
                                .option(number("fonttweaks.button", "fonttweaks.ui.desc.button",
                                        () -> cfg.buttonScale, value -> cfg.buttonScale = value))
                                .option(number("fonttweaks.chat", "fonttweaks.ui.desc.chat",
                                        () -> cfg.chatScale, value -> cfg.chatScale = value))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("fonttweaks.ui.group.spacing"))
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("fonttweaks.letter_spacing"))
                                        .description(description("fonttweaks.ui.desc.letter_spacing"))
                                        .binding(0.0, () -> cfg.letterSpacing,
                                                value -> cfg.letterSpacing = value)
                                        .controller(option -> DoubleFieldControllerBuilder.create(option)
                                                .range(-2.0, 4.0))
                                        .build())
                                .option(number("fonttweaks.global_spacing",
                                        "fonttweaks.ui.desc.global_line",
                                        () -> cfg.globalLineSpacing,
                                        value -> cfg.globalLineSpacing = value))
                                .option(number("fonttweaks.spacing",
                                        "fonttweaks.ui.desc.wrapped",
                                        () -> cfg.wrappedLineSpacing,
                                        value -> cfg.wrappedLineSpacing = value))
                                .option(number("fonttweaks.tooltip_spacing",
                                        "fonttweaks.ui.desc.tooltip",
                                        () -> cfg.tooltipLineSpacing,
                                        value -> cfg.tooltipLineSpacing = value))
                                .build())
                        .build())
                // Fonts tab: pixel effects and font replacement in one place.
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.fonts"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("fonttweaks.ui.group.pixels"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("fonttweaks.ui.pixel_toggle"))
                                        .description(illustrated("fonttweaks.ui.desc.pixel_toggle"))
                                        .binding(true, () -> cfg.pixelationEnabled,
                                                value -> cfg.pixelationEnabled = value)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())
                                .option(Option.<PixelSizeChoice>createBuilder()
                                        .name(Component.translatable("fonttweaks.ui.dot_count"))
                                        .description(illustrated("fonttweaks.ui.desc.pixel_auto"))
                                        .binding(PixelSizeChoice.ONE,
                                                () -> PixelSizeChoice.fromPixels(cfg.pixelBlockSize),
                                                value -> cfg.pixelBlockSize = value.pixels)
                                        .controller(option -> EnumControllerBuilder.create(option)
                                                .enumClass(PixelSizeChoice.class))
                                        .build())
                                .option(toggle("fonttweaks.gui_scale_font",
                                        "fonttweaks.ui.desc.gui_scale_font", false,
                                        () -> cfg.guiScaleDependentFont,
                                        value -> cfg.guiScaleDependentFont = value))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("fonttweaks.ui.group.fonts"))
                                .option(toggle("fonttweaks.ui.font_enable",
                                        "fonttweaks.ui.desc.font_enable", false,
                                        () -> cfg.resourcePackEnabled, value -> cfg.resourcePackEnabled = value))
                                .option(Option.<FontChoice>createBuilder()
                                        .name(Component.translatable("fonttweaks.ui.font_choice"))
                                        .description(illustrated("fonttweaks.ui.desc.font_choice"))
                                        .binding(FontChoice.PIXELMPLUS,
                                                () -> FontChoice.fromId(cfg.resourcePackFont),
                                                value -> cfg.resourcePackFont = value.id)
                                        .controller(option -> EnumControllerBuilder.create(option)
                                                .enumClass(FontChoice.class))
                                        .build())
                                .option(toggle("fonttweaks.ui.font_english",
                                        "fonttweaks.ui.desc.font_english", false,
                                        () -> cfg.replaceEnglish, value -> cfg.replaceEnglish = value))
                                .build())
                        .build())
                // Single exclusions tab with YACL's scrollable list editor.
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.blacklist"))
                        .option(ListOption.<String>createBuilder()
                                .name(Component.translatable("fonttweaks.blacklist"))
                                .description(description("fonttweaks.ui.desc.blacklist"))
                                .binding(List.of(),
                                        () -> new ArrayList<>(cfg.blacklistedEntries),
                                        values -> cfg.blacklistedEntries = new ArrayList<>(values))
                                .controller(StringControllerBuilder::create)
                                .initial("")
                                .build())
                        .build())
                .save(() -> {
                    // Compare baked atlas output, not the raw enum sentinel:
                    // Auto (0) and manual 2 use identical GUI3 texture pixels.
                    boolean mustReload = initialPixels != cfg.pixelationEnabled
                            || (cfg.pixelationEnabled
                                    && initialRasterPixels != cfg.effectivePixelBlockSize());
                    FontTweaksSettings.save();
                    if (mustReload) FontTweaksReload.request();
                    Minecraft mc = Minecraft.getInstance();
                    if (mc != null && mc.gui != null) mc.gui.hud.getChat().rescaleChat();
                })
                .build()
                .generateScreen(parent);
    }
}
