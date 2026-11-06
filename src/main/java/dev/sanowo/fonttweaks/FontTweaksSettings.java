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
    /** Bitmap effect only, independent of all text sizing. */
    @SerializedName(value = "pixelationEnabled", alternate = {"pixelPerfectMode"})
    public boolean pixelationEnabled = true;
    /** Physical display pixels per dot at text scale 100%; GUI scale is compensated. */
    @SerializedName(value = "pixelBlockSize", alternate = {"pixelsPerFontDot"})
    public int pixelBlockSize = 2;
    /** Relative distance between wrapped text lines; does not replace vanilla chat settings. */
    public double wrappedLineSpacing = 1.0;
    /** Vertical spacing for individual item-tooltip text lines. */
    public double tooltipLineSpacing = 1.0;

    public static FontTweaksSettings get() {
        return INSTANCE;
    }

    public float scaleFor(boolean screenOpen) {
        return (float) bounded(globalScale * (screenOpen ? screenScale : hudScale), 0.25, 4.0);
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

    public void sanitize() {
        globalScale = bounded(globalScale, 0.5, 2.0);
        screenScale = bounded(screenScale, 0.5, 2.0);
        hudScale = bounded(hudScale, 0.5, 2.0);
        buttonScale = bounded(buttonScale, 0.5, 2.0);
        chatScale = bounded(chatScale, 0.5, 2.0);
        if (pixelBlockSize < 1 || pixelBlockSize > 3) pixelBlockSize = 2;
        wrappedLineSpacing = bounded(wrappedLineSpacing, 0.5, 2.0);
        tooltipLineSpacing = bounded(tooltipLineSpacing, 0.5, 2.0);
    }

    public static double bounded(double value, double min, double max) {
        if (!Double.isFinite(value)) return 1.0;
        return Math.min(max, Math.max(min, value));
    }
}
