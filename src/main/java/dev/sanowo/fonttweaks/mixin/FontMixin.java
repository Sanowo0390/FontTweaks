package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sanowo.fonttweaks.FontTweaksFonts;
import dev.sanowo.fonttweaks.FontTweaksLetterSpacing;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Font.class)
public abstract class FontMixin {
    @ModifyConstant(method = "wordWrapHeight", constant = @Constant(intValue = 9))
    private int fonttweaks$wrappedHeight(int original) {
        return Math.max(1, (int) Math.round(original * FontTweaksSettings.get().wrappedSpacing()));
    }

    // Match StringSplitter's measurement to actual glyph positioning.
    // MC 26.3 constructs its width provider inside Font.lambda$new$0.
    @ModifyExpressionValue(method = "lambda$new$0",
            at = @At(value = "INVOKE",
                target = "Lcom/mojang/blaze3d/font/GlyphInfo;getAdvance(Z)F"))
    private float fonttweaks$horizontalTrackingWidth(float original, int codepoint, Style style) {
        return FontTweaksLetterSpacing.apply(original, codepoint);
    }

    // Rendering and StringSplitter width measurement must choose the same font per codepoint.
    @ModifyExpressionValue(method = {"getGlyph", "lambda$new$0"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Style;getFont()Lnet/minecraft/network/chat/FontDescription;"))
    private FontDescription fonttweaks$selectFont(FontDescription requested, int codepoint, Style style) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return requested;
        return FontTweaksFonts.select(requested, codepoint, FontTweaksSettings.get(),
                mc.getWindow().getGuiScale(), mc.options.forceUnicodeFont().get());
    }
}
