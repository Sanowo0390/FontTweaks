package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.sanowo.fonttweaks.FontTweaksLetterSpacing;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;

/**
 * The vanilla FormattedCharSink knows the source codepoint, but the internal
 * BakedGlyph overload doesn't. Propagate it over the nested call so the same
 * English exclusion is used for rendered glyph advances and measured widths.
 */
@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class FontPreparedTextBuilderMixin {
    @Unique private static final ThreadLocal<Integer> fonttweaks$codepoint =
            ThreadLocal.withInitial(() -> -1);

    @WrapMethod(method = "accept(ILnet/minecraft/network/chat/Style;I)Z")
    private boolean fonttweaks$withCodepoint(int position, Style style, int codepoint,
                                             Operation<Boolean> original) {
        int old = fonttweaks$codepoint.get();
        fonttweaks$codepoint.set(codepoint);
        try {
            return original.call(position, style, codepoint);
        } finally {
            fonttweaks$codepoint.set(old);
        }
    }

    @ModifyExpressionValue(
        method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z",
        at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/font/GlyphInfo;getAdvance(Z)F"))
    private float fonttweaks$horizontalTrackingDraw(float original,
                                                     int position, Style style, BakedGlyph glyph) {
        return FontTweaksLetterSpacing.apply(original, fonttweaks$codepoint.get());
    }
}
