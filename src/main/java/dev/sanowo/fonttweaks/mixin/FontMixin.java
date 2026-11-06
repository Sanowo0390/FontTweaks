package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksFonts;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FontDescription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public abstract class FontMixin {
    // Changing the description before the provider lookup also changes its
    // cache key. Both fonts stay loaded; no resource reload is needed.
    @ModifyVariable(method = "getGlyphSource", at = @At("HEAD"), argsOnly = true)
    private FontDescription fonttweaks$selectFont(FontDescription requested) {
        return FontTweaksFonts.select(requested, FontTweaksSettings.get().pixelPerfectMode);
    }
}
