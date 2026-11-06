package dev.sanowo.fonttweaks.mixin;

import com.mojang.blaze3d.font.GlyphBitmap;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.Minecraft;
import dev.sanowo.fonttweaks.PixelatedGlyphBitmap;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.client.gui.font.providers.UnihexProvider$Glyph")
public abstract class UnihexBitmapMixin {
    @Shadow @Final private UnihexProvider.LineData contents;
    @Shadow @Final private int left;

    @ModifyArg(method = "bake", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/font/UnbakedGlyph$Stitcher;stitch(Lcom/mojang/blaze3d/font/GlyphInfo;Lcom/mojang/blaze3d/font/GlyphBitmap;)Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;"), index = 1)
    private GlyphBitmap fonttweaks$pixelate(GlyphBitmap bitmap) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        Minecraft mc = Minecraft.getInstance();
        int guiScale = mc == null ? 3 : mc.getWindow().getGuiScale();
        if (!cfg.shouldPixelateAt(guiScale)) return bitmap;
        int[] rows = new int[16];
        for (int i = 0; i < 16; i++) rows[i] = contents.line(i);
        // The processed glyph's atlas resolution must not depend on the
        // current GUI scale. Otherwise every GUI resize requires a full
        // resource reload to recreate cached baked glyphs.
        return PixelatedGlyphBitmap.process(bitmap, rows, left, cfg, guiScale);
    }
}
