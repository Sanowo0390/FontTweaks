package dev.sanowo.fonttweaks;

import com.mojang.blaze3d.font.UnbakedGlyph;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

/** Carry the codepoint until the lazy atlas bake, including reloads after exclusions change. */
public final class CharacterGlyph implements UnbakedGlyph {
    private final UnbakedGlyph original;
    private final int codepoint;
    public CharacterGlyph(UnbakedGlyph original, int codepoint) {
        this.original = original;
        this.codepoint = codepoint;
    }
    public GlyphInfo info() { return original.info(); }
    public BakedGlyph bake(Stitcher stitcher) {
        boolean old = GlyphProcessingContext.push(FontTweaksSettings.get().isBlacklisted(codepoint));
        try { return original.bake(stitcher); }
        finally { GlyphProcessingContext.pop(old); }
    }
}
