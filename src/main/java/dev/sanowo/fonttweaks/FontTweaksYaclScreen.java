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
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
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
        final int initialDotSize = cfg.pixelBlockSize;

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("fonttweaks.ui.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.size"))
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
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.spacing"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("fonttweaks.ui.group.spacing"))
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
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.pixels"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("fonttweaks.ui.group.pixels"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("fonttweaks.ui.pixel_toggle"))
                                        .description(illustrated("fonttweaks.ui.desc.pixel_toggle"))
                                        .binding(true, () -> cfg.pixelationEnabled,
                                                value -> cfg.pixelationEnabled = value)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("fonttweaks.ui.dot_count"))
                                        .description(illustrated("fonttweaks.ui.desc.pixel_count"))
                                        .binding(2, () -> cfg.pixelBlockSize,
                                                value -> cfg.pixelBlockSize = value)
                                        .controller(option -> IntegerSliderControllerBuilder.create(option)
                                                .range(1, 4).step(1)
                                                .formatValue(v -> Component.literal(v + " px")))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.fonts"))
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
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("fonttweaks.ui.tab.blacklist"))
                        // YACL handles each entry as an independent editable row
                        // with its own delete/reorder controls and a scroll pane.
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
                    // YACL applies all pending values to their bindings before
                    // calling save. Cancel discards them automatically.
                    FontTweaksSettings.save();
                    if (initialPixels != cfg.pixelationEnabled
                            || (cfg.pixelationEnabled && initialDotSize != cfg.pixelBlockSize)) {
                        FontTweaksReload.request();
                    }
                    Minecraft mc = Minecraft.getInstance();
                    if (mc != null && mc.gui != null) mc.gui.hud.getChat().rescaleChat();
                })
                .build()
                .generateScreen(parent);
    }
}
