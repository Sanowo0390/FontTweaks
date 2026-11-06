package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sanowo.fonttweaks.FontTweaksFonts;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Font.class)
public abstract class FontMixin {
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
