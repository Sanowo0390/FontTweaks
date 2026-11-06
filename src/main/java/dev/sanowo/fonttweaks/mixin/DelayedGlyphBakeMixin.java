package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.font.UnbakedGlyph;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import dev.sanowo.fonttweaks.UnifontGlyph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Cache at most five images per used Unihex glyph, within the owning font atlas lifetime. */
@Mixin(targets = "net.minecraft.client.gui.font.FontSet$DelayedBake")
public abstract class DelayedGlyphBakeMixin {
    @Shadow @Final private UnbakedGlyph unbaked;
    @Shadow private BakedGlyph baked;
    @Unique private BakedGlyph[] fonttweaks$variants;

    @WrapMethod(method = "get()Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;")
    private BakedGlyph fonttweaks$selectVariant(Operation<BakedGlyph> original) {
        if (!(unbaked instanceof UnifontGlyph)) return original.call();
        Minecraft client = Minecraft.getInstance();
        int guiScale = client == null ? 3 : client.getWindow().getGuiScale();
        int signature = FontTweaksSettings.get().pixelRasterSignature(guiScale);
        if (fonttweaks$variants == null) fonttweaks$variants = new BakedGlyph[5];
        BakedGlyph cached = fonttweaks$variants[signature];
        if (cached != null) return cached;

        // Reuse vanilla's stitcher and missing-glyph handling. Only this glyph's
        // new variant is uploaded; no font/resource reload or atlas reset occurs.
        baked = null;
        BakedGlyph result = original.call();
        fonttweaks$variants[signature] = result;
        return result;
    }
}
