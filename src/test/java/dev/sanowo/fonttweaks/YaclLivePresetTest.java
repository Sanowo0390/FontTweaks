package dev.sanowo.fonttweaks;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Real YACL options must refresh within the SAME screen, without a save/reopen. */
class YaclLivePresetTest {
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void selectEnum(Option<?> option, String value) {
        Enum<?> current = (Enum<?>) option.pendingValue();
        Enum<?> next = Arrays.stream((Enum<?>[]) current.getDeclaringClass().getEnumConstants())
                .filter(item -> item.name().equals(value))
                .findFirst().orElseThrow();
        ((Option) option).requestSet(next);
    }

    @SuppressWarnings("unchecked")
    private static void selectBool(Option<?> option, boolean value) {
        ((Option<Boolean>) option).requestSet(value);
    }

    private static String enumName(Option<?> option) {
        return ((Enum<?>) option.pendingValue()).name();
    }

    private static List<OptionGroup> guiGroups(YetAnotherConfigLib config) {
        ConfigCategory fonts = config.categories().get(0);
        return fonts.groups().stream().filter(group -> group.options().size() == 4).toList();
    }

    @Test
    void selectingPresetUpdatesAllFourProfileRowsImmediatelyWithoutCommitting() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        YetAnotherConfigLib yacl = FontTweaksYaclScreen.buildConfig(cfg, 3);
        List<OptionGroup> gui = guiGroups(yacl);
        assertEquals(4, gui.size());

        Option<?> preset = yacl.categories().get(0).groups().stream()
                .flatMap(group -> group.options().stream())
                .filter(option -> option.pendingValue() instanceof Enum<?>
                        && "CUSTOM".equals(enumName(option)))
                .findFirst().orElseThrow();

        assertEquals("CUSTOM", enumName(preset));
        selectEnum(preset, "PIXEL_AND_FONTS");
        assertEquals("PIXEL_AND_FONTS", enumName(preset));
        // These are the live YACL values, not a newly re-opened screen.
        assertEquals("THREE", enumName(gui.get(0).options().get(0)));
        assertEquals(Boolean.TRUE, gui.get(0).options().get(1).pendingValue());
        assertEquals("TWO", enumName(gui.get(2).options().get(0)));
        assertEquals(Boolean.TRUE, gui.get(2).options().get(1).pendingValue());
        assertEquals(Boolean.FALSE, gui.get(3).options().get(2).pendingValue());
        assertEquals(Boolean.FALSE, gui.get(0).options().get(3).pendingValue());

        selectEnum(preset, "PIXEL_CORRECTION");
        assertEquals("ONE", enumName(gui.get(0).options().get(0)));
        assertEquals("ONE", enumName(gui.get(2).options().get(0)));
        assertEquals(Boolean.TRUE, gui.get(2).options().get(2).pendingValue());
        assertEquals(Boolean.FALSE, gui.get(0).options().get(1).pendingValue());

        selectEnum(preset, "ALWAYS_MISAKI");
        assertEquals("THREE", enumName(gui.get(3).options().get(0)));
        assertEquals(Boolean.TRUE, gui.get(3).options().get(1).pendingValue());
        assertEquals(Boolean.TRUE, gui.get(3).options().get(2).pendingValue());
        assertEquals(Boolean.FALSE, gui.get(3).options().get(3).pendingValue());

        // Nothing has been applied to the backing config: Cancel remains safe.
        assertEquals(FontTweaksPresets.CUSTOM, cfg.activePreset);
        assertEquals(1, cfg.profileFor(1).pixelBlockSize);
        assertFalse(cfg.profileFor(4).useExistingFont);
        assertTrue(cfg.unifontProcessingAt(4));
    }

    @Test
    void manuallyEditingProfileChangesPresetLabelToCustomImmediately() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        YetAnotherConfigLib yacl = FontTweaksYaclScreen.buildConfig(cfg, 3);
        ConfigCategory fonts = yacl.categories().get(0);
        List<OptionGroup> gui = guiGroups(yacl);
        Option<?> preset = fonts.groups().stream()
                .flatMap(group -> group.options().stream())
                .filter(option -> option.pendingValue() instanceof Enum<?>
                        && "CUSTOM".equals(enumName(option)))
                .findFirst().orElseThrow();

        selectEnum(preset, "ALWAYS_MISAKI");
        assertEquals("ALWAYS_MISAKI", enumName(preset));
        selectBool(gui.get(1).options().get(2), false); // GUI2: disable Unifont.
        assertEquals("CUSTOM", enumName(preset));
        assertEquals(Boolean.FALSE, gui.get(1).options().get(2).pendingValue());
        assertEquals(Boolean.TRUE, gui.get(3).options().get(2).pendingValue());
        // Selecting a new preset again still works without closing the GUI.
        selectEnum(preset, "PIXEL_AND_FONTS");
        assertEquals("PIXEL_AND_FONTS", enumName(preset));
        assertEquals(Boolean.TRUE, gui.get(2).options().get(1).pendingValue());
    }
}
