package dev.sanowo.fonttweaks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FontTweaksRenderContextTest {
    @Test
    void previewContextIsScopedAndRestored() {
        assertEquals(FontTweaksRenderContext.Kind.DEFAULT, FontTweaksRenderContext.current());
        FontTweaksRenderContext.Kind before =
                FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
        try {
            assertEquals(FontTweaksRenderContext.Kind.PREVIEW, FontTweaksRenderContext.current());
            FontTweaksRenderContext.Kind nested =
                    FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.BUTTON);
            try {
                assertEquals(FontTweaksRenderContext.Kind.BUTTON, FontTweaksRenderContext.current());
            } finally {
                FontTweaksRenderContext.pop(nested);
            }
            assertEquals(FontTweaksRenderContext.Kind.PREVIEW, FontTweaksRenderContext.current());
        } finally {
            FontTweaksRenderContext.pop(before);
        }
        assertEquals(FontTweaksRenderContext.Kind.DEFAULT, FontTweaksRenderContext.current());
    }
}

