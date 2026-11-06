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
    private static Option<?> presetOption(YetAnotherConfigLib config) {
        return config.categories().get(0).groups().get(0).options().get(0);
    }

    private static void save(YetAnotherConfigLib config) {
        config.categories().forEach(category -> category.groups().forEach(group ->
                group.options().forEach(Option::applyValue)));
        config.saveFunction().run();
    }

    @Test
    void everyPresetSurvivesSaveSyncSerializationAndReopening() {
        for (String name : List.of("PIXEL_CORRECTION", "PIXEL_AND_FONTS", "ALWAYS_MISAKI")) {
            FontTweaksSettings cfg = new FontTweaksSettings();
            YetAnotherConfigLib config = FontTweaksYaclScreen.buildConfig(cfg, 3, () -> {});
            Option<?> preset = presetOption(config);
            selectEnum(preset, name);
            save(config);
            assertEquals(name, enumName(preset), "after saving");
            String savedId = cfg.activePreset;
            assertNotEquals(FontTweaksPresets.CUSTOM, savedId);
            // Repeated save/apply/sync, including unchanged controls, must not
            // turn a preset into custom as the real YACL save path runs.
            guiGroups(config).forEach(group -> group.options().forEach(Option::forgetPendingValue));
            assertEquals(name, enumName(preset), "after synchronizing unchanged rows");
            save(config);
            assertEquals(savedId, cfg.activePreset);
            com.google.gson.Gson gson = new com.google.gson.Gson();
            FontTweaksSettings restored = gson.fromJson(gson.toJson(cfg), FontTweaksSettings.class);
            restored.sanitize();
            YetAnotherConfigLib reopened = FontTweaksYaclScreen.buildConfig(restored, 2, () -> {});
            assertEquals(name, enumName(presetOption(reopened)), "after reopening at another GUI size");
        }
    }

    @Test
    void redundantEventsAndUnrelatedTextChangesKeepPresetButRealProfileEditDoesNot() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        FontTweaksPresets.apply(cfg, FontTweaksPresets.ALWAYS_MISAKI);
        YetAnotherConfigLib config = FontTweaksYaclScreen.buildConfig(cfg, 3, () -> {});
        Option<?> preset = presetOption(config);
        selectBool(guiGroups(config).get(1).options().get(2), true);
        assertEquals("ALWAYS_MISAKI", enumName(preset));
        @SuppressWarnings("unchecked")
        Option<Double> size = (Option<Double>) config.categories().get(1).groups().get(0).options().get(0);
        size.requestSet(1.25);
        save(config);
        assertEquals(FontTweaksPresets.ALWAYS_MISAKI, cfg.activePreset);
        assertEquals(1.25, cfg.globalScale);
        selectBool(guiGroups(config).get(1).options().get(2), false);
        assertEquals("CUSTOM", enumName(preset));
        save(config);
        assertEquals(FontTweaksPresets.CUSTOM, cfg.activePreset);
        assertFalse(cfg.unifontProcessingAt(2));
    }

    @Test
    void savingPresetClearsInvisibleLegacyFontOverride() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        FontTweaksPresets.apply(cfg, FontTweaksPresets.PIXEL_AND_FONTS);
        cfg.profileFor(3).legacyExistingFont = "misaki";
        YetAnotherConfigLib config = FontTweaksYaclScreen.buildConfig(cfg, 3, () -> {});
        save(config);
        assertNull(cfg.profileFor(3).legacyExistingFont);
        assertSame(FontTweaksFonts.PIXELMPLUS, FontTweaksFonts.select(
                net.minecraft.network.chat.FontDescription.DEFAULT, '日', cfg, 3, false));
    }

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

