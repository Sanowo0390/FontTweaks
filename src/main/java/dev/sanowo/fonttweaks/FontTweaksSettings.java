package dev.sanowo.fonttweaks;

import com.google.gson.Gson;
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
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fonttweaks.json");
    private static volatile FontTweaksSettings INSTANCE = new FontTweaksSettings();

    /** Scale applied to the HUD and screen text (except the mod's own config screen). */
    public double globalScale = 1.0;
    /** Extra multiplier for main menu, inventory, and other screen text. */
    public double screenScale = 1.0;
    /** Extra multiplier when no screen is open, including the HUD. */
    public double hudScale = 1.0;
    public double buttonScale = 1.0;
    public double chatScale = 1.0;
    /** Align native 8x8 Misaki Gothic and vanilla bitmap texels to physical pixels at GUI scale 3. */
    public boolean pixelPerfectMode = true;
    /** Physical screen pixels per 8x8 font texel when GUI scale is 3. Default: exactly 2x2. */
    public int pixelsPerFontDot = 2;
    /** Relative distance between wrapped text lines; does not replace vanilla chat settings. */
    public double wrappedLineSpacing = 1.0;
    /** Vertical spacing for individual item-tooltip text lines. */
    public double tooltipLineSpacing = 1.0;

    public static FontTweaksSettings get() {
        return INSTANCE;
    }

    public boolean isPixelModeActive(int guiScale) {
        return pixelPerfectMode && guiScale == 3;
    }

    public double targetGuiFontScale(int guiScale) {
        return (double) pixelsPerFontDot / guiScale;
    }

    public float scaleFor(boolean screenOpen) {
        return (float) bounded(globalScale * (screenOpen ? screenScale : hudScale), 0.25, 4.0);
    }

    public static synchronized void load() {
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

    public void sanitize() {
        globalScale = bounded(globalScale, 0.5, 2.0);
        screenScale = bounded(screenScale, 0.5, 2.0);
        hudScale = bounded(hudScale, 0.5, 2.0);
        buttonScale = bounded(buttonScale, 0.5, 2.0);
        chatScale = bounded(chatScale, 0.5, 2.0);
        if (pixelsPerFontDot < 1 || pixelsPerFontDot > 3) pixelsPerFontDot = 2;
        wrappedLineSpacing = bounded(wrappedLineSpacing, 0.5, 2.0);
        tooltipLineSpacing = bounded(tooltipLineSpacing, 0.5, 2.0);
    }

    public static double bounded(double value, double min, double max) {
        if (!Double.isFinite(value)) return 1.0;
        return Math.min(max, Math.max(min, value));
    }
}
