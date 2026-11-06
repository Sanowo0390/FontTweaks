package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksFonts;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public abstract class FontMixin {
    @ModifyVariable(method = "getGlyphSource", at = @At("HEAD"), argsOnly = true)
    private FontDescription fonttweaks$selectFont(FontDescription requested) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return requested;
        FontTweaksSettings cfg = FontTweaksSettings.get();
        return FontTweaksFonts.select(requested, FontTweaksFonts.matchesEnglishGrid(
                cfg.pixelationEnabled, cfg.pixelBlockSize, mc.getWindow().getGuiScale(),
                mc.options.forceUnicodeFont().get()));
    }
}
