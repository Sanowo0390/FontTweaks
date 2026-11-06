package dev.sanowo.fonttweaks;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FontTweaksFontsTest {
    @Test
    void defaultFontSwitchesBackOnEveryToggle() {
        FontDescription original = new FontDescription.Resource(
                Identifier.fromNamespaceAndPath("minecraft", "default"));
        for (int i = 0; i < 5; i++) {
            assertSame(FontTweaksFonts.MISAKI, FontTweaksFonts.select(original, true));
            assertSame(original, FontTweaksFonts.select(original, false));
        }
    }

    @Test
    void explicitCustomFontsArePreserved() {
        FontDescription custom = new FontDescription.Resource(
                Identifier.fromNamespaceAndPath("example", "icons"));
        assertSame(custom, FontTweaksFonts.select(custom, true));
        assertSame(custom, FontTweaksFonts.select(custom, false));
        assertSame(FontTweaksFonts.MISAKI,
                FontTweaksFonts.select(FontTweaksFonts.MISAKI, true));
    }

    @Test
    void offDisablesPixelModeAndSavedScalesWithoutErasingThem() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        cfg.globalScale = 2.0 / 3.0;
        cfg.screenScale = 1.5;
        cfg.pixelPerfectMode = false;
        assertEquals(1.0f, cfg.scaleFor(true));
        assertEquals(1.0f, cfg.scaleFor(false));
        for (int gui = 1; gui <= 4; gui++) assertFalse(cfg.isPixelModeActive(gui));
        assertEquals(2.0 / 3.0, cfg.globalScale);
    }

    @Test
    void enabledPixelSizesApplyOnlyAtGuiThree() {
        FontTweaksSettings cfg = new FontTweaksSettings();
        assertTrue(cfg.pixelPerfectMode);
        assertEquals(2, cfg.pixelsPerFontDot);
        assertFalse(cfg.isPixelModeActive(2));
        assertTrue(cfg.isPixelModeActive(3));
        assertFalse(cfg.isPixelModeActive(4));
        for (int px = 1; px <= 3; px++) {
            cfg.pixelsPerFontDot = px;
            assertEquals(px, cfg.targetGuiFontScale(3) * 3, 1e-12);
        }
    }
}
