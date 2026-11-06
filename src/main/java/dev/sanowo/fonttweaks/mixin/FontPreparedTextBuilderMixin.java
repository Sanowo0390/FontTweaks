package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sanowo.fonttweaks.FontTweaksLetterSpacing;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Advances the actual draw pen horizontally by the same spacing that
 * FontMixin adds to StringSplitter measurements. The glyph quad itself
 * is unchanged, so this adjusts letter spacing, not line spacing or size.
 */
@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class FontPreparedTextBuilderMixin {
    @ModifyExpressionValue(method =
            "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
            at = @At(value = "INVOKE",
                target = "Lcom/mojang/blaze3d/font/GlyphInfo;getAdvance(Z)F"))
    private float fonttweaks$horizontalTrackingDraw(float original,
                                                     int position, Style style, BakedGlyph glyph) {
        return FontTweaksLetterSpacing.apply(original);
    }
}
