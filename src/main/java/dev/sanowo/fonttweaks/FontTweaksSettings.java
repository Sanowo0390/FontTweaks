package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FontTweaksSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("fonttweaks.json");
    }
    private static volatile FontTweaksSettings INSTANCE = new FontTweaksSettings();

    /** Scale applied to the HUD and screen text (except the mod's own config screen). */
    public double globalScale = 1.0;
    /** Extra multiplier for main menu, inventory, and other screen text. */
    public double screenScale = 1.0;
    /** Extra multiplier when no screen is open, including the HUD. */
    public double hudScale = 1.0;
    public double buttonScale = 1.0;
    public double chatScale = 1.0;
    /** Horizontal glyph tracking in GUI units, added to each glyph advance. */
    public double letterSpacing = 0.0;
    /** Leave Latin glyphs at their native letter advance. */
    public boolean excludeEnglishFromSpacing = false;
    public boolean resourcePackEnabled = false;
    public String resourcePackFont = "pixelmplus12";
    public boolean replaceEnglish = false;
    /** Use bundled Misaki Gothic when Minecraft automatically reduces GUI scale below 3. */
    public boolean guiScaleDependentFont = false;
    /** Each entry is one codepoint excluded from font replacement only. */
    public java.util.List<String> blacklistedEntries = new java.util.ArrayList<>();
    /** Read old configs, migrate on sanitize, then omit this legacy field on save. */
    @Deprecated public String blacklistedCharacters;
    /** Bitmap effect only, independent of all text sizing. */
    @SerializedName(value = "pixelationEnabled", alternate = {"pixelPerfectMode"})
    public boolean pixelationEnabled = true;
    /**
     * Source bitmap pixelation density at GUI scale 3.
     * 0 = Auto (2px at the fixed GUI3 atlas reference); 1..4 = manual.
     * Auto uses the already-baked atlas to avoid reloading on GUI resize.
     */
    @SerializedName(value = "pixelBlockSize", alternate = {"pixelsPerFontDot"})
    public int pixelBlockSize = 1;
    /** Correct pixel density only at GUI scale 3; native glyphs at GUI3 when disabled. */
    public boolean gui3DotCorrection = true;
    /** Selected preset ID or \"custom\". Existing configs are custom by default. */
    public String activePreset = FontTweaksPresets.CUSTOM;
    /** GUI1/2/3/4/5+ each has its own dot count and built-in-font switch. */
    public java.util.Map<String, GuiScaleProfile> guiScaleProfiles = new java.util.LinkedHashMap<>();

    public static int profileScale(int guiScale) {
        return Math.min(5, Math.max(1, guiScale));
    }

    public boolean hasGuiProfiles() {
        return guiScaleProfiles != null && !guiScaleProfiles.isEmpty();
    }

    /** Lazily migrates old settings as each GUI profile is first accessed. */
    public GuiScaleProfile profileFor(int guiScale) {
        if (guiScaleProfiles == null) guiScaleProfiles = new java.util.LinkedHashMap<>();
        int scale = profileScale(guiScale);
        return guiScaleProfiles.computeIfAbsent(Integer.toString(scale), key -> {
            boolean useExisting = guiScaleDependentFont ? scale < 3 : resourcePackEnabled;
            String previousFont = null;
            if (useExisting) {
                if (guiScaleDependentFont || "misaki".equals(resourcePackFont)
                        || ("auto".equals(resourcePackFont) && pixelationEnabled
                        && effectivePixelBlockSize() == scale)) previousFont = "misaki";
                else previousFont = "pixelmplus12";
            }
            return new GuiScaleProfile(pixelBlockSize, useExisting, previousFont);
        });
    }

    public int pixelBlockSizeFor(int guiScale) {
        return hasGuiProfiles() ? profileFor(guiScale).pixelBlockSize : pixelBlockSize;
    }

    public int effectivePixelBlockSize(int guiScale) {
        int size = pixelBlockSizeFor(guiScale);
        return size == AUTO_PIXEL_SIZE ? AUTO_REFERENCE_PIXEL_SIZE : size;
    }

    public boolean unifontProcessingAt(int guiScale) {
        if (!hasGuiProfiles()) return pixelationEnabled;
        Boolean enabled = profileFor(guiScale).unifontProcessing;
        return enabled != null ? enabled : pixelationEnabled;
    }

    public boolean replaceEnglishAt(int guiScale) {
        if (!hasGuiProfiles()) return replaceEnglish;
        Boolean enabled = profileFor(guiScale).replaceEnglish;
        return enabled != null ? enabled : replaceEnglish;
    }

    public void setProfileUnifontProcessing(int guiScale, boolean enabled) {
        if (unifontProcessingAt(guiScale) == enabled) return;
        profileFor(guiScale).unifontProcessing = enabled;
        activePreset = FontTweaksPresets.CUSTOM;
    }

    public void setProfileReplaceEnglish(int guiScale, boolean enabled) {
        if (replaceEnglishAt(guiScale) == enabled) return;
        profileFor(guiScale).replaceEnglish = enabled;
        activePreset = FontTweaksPresets.CUSTOM;
    }

    /** Explicit profile edits clear the old internal font choice. */
    public void setProfilePixelSize(int guiScale, int pixels) {
        GuiScaleProfile p = profileFor(guiScale);
        if (p.pixelBlockSize == pixels) return;
        p.pixelBlockSize = pixels;
        p.legacyExistingFont = null;
        activePreset = FontTweaksPresets.CUSTOM;
    }

    public void setProfileExistingFont(int guiScale, boolean enabled) {
        GuiScaleProfile p = profileFor(guiScale);
        if (p.useExistingFont == enabled) return;
        p.useExistingFont = enabled;
        p.legacyExistingFont = null;
        activePreset = FontTweaksPresets.CUSTOM;
    }

    public static final int AUTO_PIXEL_SIZE = 0;
    public static final int AUTO_REFERENCE_PIXEL_SIZE = 2;

    /** The cached Unifont atlas is always baked at GUI scale 3. */
    public int effectivePixelBlockSize() {
        return pixelBlockSize == AUTO_PIXEL_SIZE ? AUTO_REFERENCE_PIXEL_SIZE : pixelBlockSize;
    }

    public boolean automaticPixelSize() {
        return pixelBlockSize == AUTO_PIXEL_SIZE;
    }

    /** 0 = vanilla Unifont, 1..4 = a processed glyph atlas. */
    public int pixelRasterSignature(int guiScale) {
        if (!unifontProcessingAt(guiScale)) return 0;
        // At 1px, GUI 2 and 4 have a naturally aligned pixel grid.
        // Avoid overwriting their original glyph bitmap.
        if (effectivePixelBlockSize(guiScale) == 1 && (guiScale == 2 || guiScale == 4)) return 0;
        if (guiScale == 3 && !gui3DotCorrection) return 0;
        return effectivePixelBlockSize(guiScale);
    }

    public boolean shouldPixelateAt(int guiScale) {
        return pixelRasterSignature(guiScale) > 0;
    }
    /** Relative distance between wrapped text lines; does not replace vanilla chat settings. */
    public double wrappedLineSpacing = 1.0;
    /** Vertical spacing for individual item-tooltip text lines. */
    public double tooltipLineSpacing = 1.0;
    /** Shared multiplier for standard multiline text, tooltips and chat. */
    public double globalLineSpacing = 1.0;

    public double wrappedSpacing() { return globalLineSpacing * wrappedLineSpacing; }
    public double tooltipSpacing() { return globalLineSpacing * tooltipLineSpacing; }
    public int lineStep(int original) {
        return Math.max(1, (int) Math.round(original * globalLineSpacing));
    }
    public double chatSpacingOption(double vanilla) {
        return (vanilla + 1.0) * globalLineSpacing - 1.0;
    }
    public double buttonTextScale() { return bounded(globalScale * screenScale * buttonScale, 0.05, 8.0); }
    public double chatTextScale() { return bounded(globalScale * chatScale, 0.05, 8.0); }

    public static FontTweaksSettings get() {
        return INSTANCE;
    }

    public int nextPixelBlockSize() {
        return pixelBlockSize >= 4 ? AUTO_PIXEL_SIZE : pixelBlockSize + 1;
    }

    public void cycleResourcePackFont() {
        resourcePackFont = switch (resourcePackFont) {
            case "pixelmplus12" -> "misaki";
            case "misaki" -> "auto";
            default -> "pixelmplus12";
        };
    }

    public float scaleFor(boolean screenOpen) {
        return (float) bounded(globalScale * (screenOpen ? screenScale : hudScale), 0.25, 4.0);
    }

    public boolean isBlacklisted(int codepoint) {
        return (blacklistedEntries != null && blacklistedEntries.contains(new String(Character.toChars(codepoint))))
                || (blacklistedCharacters != null && blacklistedCharacters.indexOf(codepoint) >= 0);
    }

    public boolean addBlacklistCharacters(String text) {
        if (blacklistedEntries == null) blacklistedEntries = new java.util.ArrayList<>();
        int before = blacklistedEntries.size();
        text.codePoints().forEach(cp -> {
            String entry = new String(Character.toChars(cp));
            if (!blacklistedEntries.contains(entry)) blacklistedEntries.add(entry);
        });
        return before != blacklistedEntries.size();
    }

    public void clearBlacklist() {
        blacklistedEntries = new java.util.ArrayList<>();
        blacklistedCharacters = null;
    }

    public boolean shouldReplace(int codepoint) {
        return !isBlacklisted(codepoint) && (replaceEnglish || !isEnglish(codepoint));
    }

    public static boolean isEnglish(int codepoint) {
        return (codepoint >= 0x20 && codepoint <= 0x7E)
                || Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.LATIN;
    }

    public static synchronized void load() {
        Path FILE = configFile();
        try {
            if (Files.isRegularFile(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                    FontTweaksSettings loaded = GSON.fromJson(reader, FontTweaksSettings.class);
                    if (loaded != null) {
                        loaded.sanitize();
                        INSTANCE = loaded;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FontTweaks] Could not load config, using defaults: " + e);
        }
    }

    public static synchronized void save() {
        Path FILE = configFile();
        INSTANCE.sanitize();
        try {
            Files.createDirectories(FILE.getParent());
            Path temp = FILE.resolveSibling(FILE.getFileName().toString() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(INSTANCE, writer);
            }
            try {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("[FontTweaks] Could not save config: " + e);
        }
    }

    public static synchronized void reset() {
        INSTANCE = new FontTweaksSettings();
        save();
    }
    /** A deep copy so the settings UI can restore all values, including blacklist entries. */
    public static synchronized FontTweaksSettings snapshot() {
        return GSON.fromJson(GSON.toJson(INSTANCE), FontTweaksSettings.class);
    }

    /** Restore the copy captured when the user opened the settings dashboard. */
    public static synchronized void restore(FontTweaksSettings snapshot) {
        if (snapshot == null) return;
        INSTANCE = GSON.fromJson(GSON.toJson(snapshot), FontTweaksSettings.class);
        INSTANCE.sanitize();
        save();
    }


    public void sanitize() {
        if (!"pixelmplus12".equals(resourcePackFont) && !"misaki".equals(resourcePackFont)
                && !"auto".equals(resourcePackFont)) resourcePackFont = "pixelmplus12";
        java.util.List<String> oldEntries = blacklistedEntries;
        blacklistedEntries = new java.util.ArrayList<>();
        if (oldEntries != null) for (String entry : oldEntries) if (entry != null) addBlacklistCharacters(entry);
        if (blacklistedCharacters != null) addBlacklistCharacters(blacklistedCharacters);
        blacklistedCharacters = null;
        globalScale = bounded(globalScale, 0.5, 2.0);
        screenScale = bounded(screenScale, 0.5, 2.0);
        hudScale = bounded(hudScale, 0.5, 2.0);
        buttonScale = bounded(buttonScale, 0.5, 2.0);
        chatScale = bounded(chatScale, 0.5, 2.0);
        // Tracking is additive, so its neutral value is 0 (not the multiplier default 1).
        letterSpacing = Double.isFinite(letterSpacing)
                ? bounded(letterSpacing, -2.0, 4.0) : 0.0;
        if (pixelBlockSize < AUTO_PIXEL_SIZE || pixelBlockSize > 4) pixelBlockSize = 1;
        if (!FontTweaksPresets.isValid(activePreset)) {
            activePreset = FontTweaksPresets.CUSTOM;
        }
        if (guiScaleProfiles == null) guiScaleProfiles = new java.util.LinkedHashMap<>();
        guiScaleProfiles.entrySet().removeIf(entry -> {
            if (entry.getValue() == null || entry.getKey() == null) return true;
            try {
                int scale = Integer.parseInt(entry.getKey());
                return scale < 1 || scale > 5;
            } catch (NumberFormatException exception) {
                return true;
            }
        });
        for (GuiScaleProfile profile : guiScaleProfiles.values()) {
            if (profile.pixelBlockSize < AUTO_PIXEL_SIZE || profile.pixelBlockSize > 4) {
                profile.pixelBlockSize = 1;
            }
            if (!"misaki".equals(profile.legacyExistingFont)
                    && !"pixelmplus12".equals(profile.legacyExistingFont)) {
                profile.legacyExistingFont = null;
            }
        }
        wrappedLineSpacing = bounded(wrappedLineSpacing, 0.5, 2.0);
        tooltipLineSpacing = bounded(tooltipLineSpacing, 0.5, 2.0);
        globalLineSpacing = bounded(globalLineSpacing, 0.5, 2.0);
    }

    public static double bounded(double value, double min, double max) {
        if (!Double.isFinite(value)) return 1.0;
        return Math.min(max, Math.max(min, value));
    }
}
