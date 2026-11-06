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
    public float globalScale = 1.0f;
    /** Extra multiplier for main menu, inventory, and other screen text. */
    public float screenScale = 1.0f;
    /** Extra multiplier when no screen is open, including the HUD. */
    public float hudScale = 1.0f;
    /** Relative distance between wrapped text lines; does not replace vanilla chat settings. */
    public float wrappedLineSpacing = 1.0f;
    /** Vertical spacing for individual item-tooltip text lines. */
    public float tooltipLineSpacing = 1.0f;

    public static FontTweaksSettings get() {
        return INSTANCE;
    }

    public float scaleFor(boolean screenOpen) {
        return bounded(globalScale * (screenOpen ? screenScale : hudScale), 0.4f, 2.5f);
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
        globalScale = bounded(globalScale, 0.5f, 2.0f);
        screenScale = bounded(screenScale, 0.5f, 2.0f);
        hudScale = bounded(hudScale, 0.5f, 2.0f);
        wrappedLineSpacing = bounded(wrappedLineSpacing, 0.75f, 2.0f);
        tooltipLineSpacing = bounded(tooltipLineSpacing, 0.75f, 2.0f);
    }

    public static float bounded(float value, float min, float max) {
        if (!Float.isFinite(value)) return 1.0f;
        return Math.min(max, Math.max(min, value));
    }
}
