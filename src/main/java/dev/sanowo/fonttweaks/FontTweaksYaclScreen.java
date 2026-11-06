package dev.sanowo.fonttweaks;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.NameableEnum;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
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

    private enum PresetChoice implements NameableEnum {
        CUSTOM(FontTweaksPresets.CUSTOM, "fonttweaks.preset.custom"),
        PIXEL_CORRECTION(FontTweaksPresets.PIXEL_CORRECTION, "fonttweaks.preset.pixel_correction"),
        PIXEL_AND_FONTS(FontTweaksPresets.PIXEL_AND_FONTS, "fonttweaks.preset.pixel_and_fonts"),
        ALWAYS_MISAKI(FontTweaksPresets.ALWAYS_MISAKI, "fonttweaks.preset.always_misaki");

        private final String id;
        private final String label;
        PresetChoice(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(label);
        }

        static PresetChoice fromId(String id) {
            for (PresetChoice choice : values()) {
                if (choice.id.equals(id)) return choice;
            }
            return CUSTOM;
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

    /**
     * Hold the actual YACL Options for each GUI profile, so a preset can
     * update their pending values immediately without rebuilding the screen.
     * All changes remain undoable until YACL's Save Changes is pressed.
     */
    private record ProfileControls(int scale, OptionGroup group,
                                   Option<PixelSizeChoice> pixels,
                                   Option<Boolean> existing,
                                   Option<Boolean> unifont,
                                   Option<Boolean> english) {
        void showPreset(GuiScaleProfile values) {
            pixels.requestSet(PixelSizeChoice.fromPixels(values.pixelBlockSize));
            existing.requestSet(values.useExistingFont);
            unifont.requestSet(Boolean.TRUE.equals(values.unifontProcessing));
            english.requestSet(Boolean.TRUE.equals(values.replaceEnglish));
        }

        boolean matches(GuiScaleProfile values) {
            return pixels.pendingValue().pixels == values.pixelBlockSize
                    && existing.pendingValue() == values.useExistingFont
                    && unifont.pendingValue().equals(values.unifontProcessing)
                    && english.pendingValue().equals(values.replaceEnglish);
        }

        void onManualChange(Runnable listener) {
            pixels.addEventListener((option, event) -> {
                if (event == OptionEventListener.Event.STATE_CHANGE) listener.run();
            });
            existing.addEventListener((option, event) -> {
                if (event == OptionEventListener.Event.STATE_CHANGE) listener.run();
            });
            unifont.addEventListener((option, event) -> {
                if (event == OptionEventListener.Event.STATE_CHANGE) listener.run();
            });
            english.addEventListener((option, event) -> {
                if (event == OptionEventListener.Event.STATE_CHANGE) listener.run();
            });
        }
    }

    /** Independent GUI1/2/3/4+ YACL groups. */
    private static ProfileControls profileGroup(FontTweaksSettings cfg, int scale) {
        cfg.profileFor(scale);
        Option<PixelSizeChoice> pixels = Option.<PixelSizeChoice>createBuilder()
                .name(Component.translatable("fonttweaks.ui.dot_count"))
                .description(illustrated("fonttweaks.ui.desc.profile_dots"))
                .binding(PixelSizeChoice.ONE,
                        () -> PixelSizeChoice.fromPixels(cfg.profileFor(scale).pixelBlockSize),
                        value -> cfg.setProfilePixelSize(scale, value.pixels))
                .controller(option -> EnumControllerBuilder.create(option)
                        .enumClass(PixelSizeChoice.class))
                .build();
        Option<Boolean> existing = toggle("fonttweaks.ui.existing_font",
                "fonttweaks.ui.desc.existing_font", false,
                () -> cfg.profileFor(scale).useExistingFont,
                value -> cfg.setProfileExistingFont(scale, value));
        Option<Boolean> unifont = toggle("fonttweaks.profile.unifont_processing",
                "fonttweaks.ui.desc.profile.unifont_processing", true,
                () -> cfg.unifontProcessingAt(scale),
                value -> cfg.setProfileUnifontProcessing(scale, value));
        Option<Boolean> english = toggle("fonttweaks.profile.replace_english",
                "fonttweaks.ui.desc.profile.replace_english", false,
                () -> cfg.replaceEnglishAt(scale),
                value -> cfg.setProfileReplaceEnglish(scale, value));
        OptionGroup group = OptionGroup.createBuilder()
                .name(Component.translatable("fonttweaks.ui.profile." + scale))
                .option(pixels).option(existing).option(unifont).option(english)
                .build();
        return new ProfileControls(scale, group, pixels, existing, unifont, english);
    }

    public static Screen create(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        int guiScale = client == null ? 3 : client.getWindow().getGuiScale();
        return buildConfig(FontTweaksSettings.get(), guiScale).generateScreen(parent);
    }

    /** Separate option-state creation from drawing so pending UI changes are testable. */
    static YetAnotherConfigLib buildConfig(FontTweaksSettings cfg, int initialGuiScale) {
        return buildConfig(cfg, initialGuiScale, FontTweaksSettings::save);
    }

    static YetAnotherConfigLib buildConfig(FontTweaksSettings cfg, int initialGuiScale, Runnable persist) {
        ProfileControls gui1 = profileGroup(cfg, 1);
        ProfileControls gui2 = profileGroup(cfg, 2);
        ProfileControls gui3 = profileGroup(cfg, 3);
        ProfileControls gui4 = profileGroup(cfg, 4);
        List<ProfileControls> profiles = List.of(gui1, gui2, gui3, gui4);

      …4827 tokens truncated…ate static boolean sourceInk(int[] rows, int left, int width, int x, int y) {
        int bit = 31 - left - x;
        return x >= 0 && x < width && y >= 0 && y < 16 && bit >= 0 && bit < 32
                && ((rows[y] >>> bit) & 1) != 0;
    }

    private static boolean safeToClear(boolean[] ink, int columns, int lines, int index) {
        int x = index % columns, y = index / columns;
        // Keep raster boundaries, isolated dots, endpoints and thin bridges.
        if (x == 0 || y == 0 || x == columns - 1 || y == lines - 1) return false;
        boolean[] neighbors = new boolean[9];
        int count = 0, start = -1;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int local = (dy + 1) * 3 + dx + 1;
                if (ink[(y + dy) * columns + x + dx]) {
                    neighbors[local] = true;
                    count++;
                    start = local;
                }
            }
        }
        if (count < 3) return false;
        int[] queue = new int[8];
        int head = 0, tail = 0;
        queue[tail++] = start;
        neighbors[start] = false;
        while (head < tail) {
            int current = queue[head++];
            for (int next = 0; next < 9; next++) {
                if (neighbors[next] && Math.abs(current % 3 - next % 3) <= 1
                        && Math.abs(current / 3 - next / 3) <= 1) {
                    neighbors[next] = false;
                    queue[tail++] = next;
                }
            }
        }
        return tail == count;
    }
}

