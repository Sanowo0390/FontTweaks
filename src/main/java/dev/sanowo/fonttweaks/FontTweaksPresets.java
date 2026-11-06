package dev.sanowo.fonttweaks;

import java.util.LinkedHashMap;

/**
 * Three simple, explicit collections of GUI-scale profile settings.
 * Pixel rasterization applies to Unifont; bundled Misaki / PixelMplus
 * carry their own font textures and are selected separately at draw time.
 */
public final class FontTweaksPresets {
    public static final String CUSTOM = "custom";
    public static final String PIXEL_CORRECTION = "pixel_correction";
    public static final String PIXEL_AND_FONTS = "pixel_and_fonts";
    public static final String ALWAYS_MISAKI = "always_misaki";

    private FontTweaksPresets() {}

    public static boolean isValid(String id) {
        return CUSTOM.equals(id) || PIXEL_CORRECTION.equals(id)
                || PIXEL_AND_FONTS.equals(id) || ALWAYS_MISAKI.equals(id);
    }

    public static void apply(FontTweaksSettings cfg, String id) {
        if (!isValid(id)) throw new IllegalArgumentException("Unknown FontTweaks preset: " + id);
        if (CUSTOM.equals(id)) {
            cfg.activePreset = CUSTOM;
            return;
        }

        LinkedHashMap<String, GuiScaleProfile> profiles = new LinkedHashMap<>();
        for (int gui = 1; gui <= 4; gui++) {
            // The unmentioned scales remain completely native, including
            // GUI 4+ in preset 2. Scale 4 covers all larger sizes.
            profiles.put(Integer.toString(gui), profile(1, false, false));
        }

        switch (id) {
            case PIXEL_CORRECTION -> {
                // Unifont's native dot occupies half a GUI unit; rounded
                // GUI1 and GUI3 both use 1px correction, per preset 1.
                profiles.put("1", profile(1, false, true));
                profiles.put("3", profile(1, false, true));
            }
            case PIXEL_AND_FONTS -> {
                // GUI1 Misaki / 3px; GUI3 PixelMplus12 / 2px.
                // GUI2 and GUI4+ retain vanilla appearance.
                profiles.put("1", profile(3, true, true));
                profiles.put("3", profile(2, true, true));
            }
            case ALWAYS_MISAKI -> {
                for (int gui = 1; gui <= 4; gui++) {
                    profiles.put(Integer.toString(gui), profile(3, true, true));
                }
            }
            default -> throw new IllegalArgumentException("Unknown FontTweaks preset: " + id);
        }

        cfg.guiScaleProfiles = profiles;
        cfg.pixelationEnabled = true;
        // Every preset must disable Latin / English glyph replacement.
        cfg.replaceEnglish = false;
        cfg.activePreset = id;
    }

    private static GuiScaleProfile profile(int pixels, boolean existing, boolean unifont) {
        GuiScaleProfile p = new GuiScaleProfile(pixels, existing, null);
        p.unifontProcessing = unifont;
        p.replaceEnglish = false;
        return p;
    }
}
