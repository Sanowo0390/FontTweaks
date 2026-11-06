package dev.sanowo.fonttweaks;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FontTweaksFontsTest {
    @Test void guiFourSelectsMisakiAtFourAndIdentifiesNativeAtTwo() {
        assertTrue(FontTweaksFonts.matchesEnglishGrid(true, 4, 4, false));
        assertFalse(FontTweaksFonts.matchesEnglishGrid(true, 2, 4, false));
        assertTrue(FontTweaksFonts.matchesNativeUnifontGrid(2, 4));
        assertFalse(FontTweaksFonts.matchesNativeUnifontGrid(2, 3));
        assertFalse(FontTweaksFonts.matchesNativeUnifontGrid(0, 0));
    }

    @Test void englishGridMatchesOnlyEqualGuiAndTarget() {
        for (int gui = 1; gui <= 4; gui++) {
            for (int target = 1; target <= 4; target++) {
                assertEquals(gui == target, FontTweaksFonts.matchesEnglishGrid(true, target, gui, false));
            }
        }
        assertFalse(FontTweaksFonts.matchesEnglishGrid(true, 3, 0, false));
    }

    @Test void offAndForcedUnicodeDoNotSelectMisaki() {
        assertFalse(FontTweaksFonts.matchesEnglishGrid(false, 3, 3, false));
        assertFalse(FontTweaksFonts.matchesEnglishGrid(true, 3, 3, true));
    }

    @Test void defaultFontSwitchesBackAndExplicitFontsStayUnchanged() {
        FontDescription custom = new FontDescription.Resource(Identifier.fromNamespaceAndPath("test", "icons"));
        for (int i = 0; i < 4; i++) {
            assertSame(FontTweaksFonts.MISAKI, FontTweaksFonts.select(FontDescription.DEFAULT, true));
            assertSame(FontDescription.DEFAULT, FontTweaksFonts.select(FontDescription.DEFAULT, false));
            assertSame(custom, FontTweaksFonts.select(custom, true));
        }
    }
}
