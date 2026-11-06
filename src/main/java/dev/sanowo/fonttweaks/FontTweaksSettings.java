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
    /** Physical display pixels per dot at text scale 100%; GUI scale is compensated. */
    @SerializedName(value = "pixelBlockSize", alternate = {"pixelsPerFontDot"})
    public int pixelBlockSize = 1;
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
        return pixelBlockSize >= 4 ? 1 : pixelBlockSize + 1;
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
        if (pixelBlockSize < 1 || pixelBlockSize > 4) pixelBlockSize = 1;
        wrappedLineSpacing = bounded(wrappedLineSpacing, 0.5, 2.0);
        tooltipLineSpacing = bounded(tooltipLineSpacing, 0.5, 2.0);
        globalLineSpacing = bounded(globalLineSpacing, 0.5, 2.0);
    }

    public static double bounded(double value, double min, double max) {
        if (!Double.isFinite(value)) return 1.0;
        return Math.min(max, Math.max(min, value));
    }
}
