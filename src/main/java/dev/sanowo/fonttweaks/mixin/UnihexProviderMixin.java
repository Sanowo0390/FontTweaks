package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksSettings;
import dev.sanowo.fonttweaks.PixelGlyphProcessor;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

/**
 * Edits the glyph's original bitmap just before the renderer uploads it to
 * the font atlas. No GUI-scale adjustment is performed.
 *
 * The private nested LineData argument is coerced to Object so the Mixin
 * can be compiled without referencing an inaccessible nested type.
 */
@Mixin(UnihexProvider.class)
public abstract class UnihexProviderMixin {
    @Inject(
        method = "unpackBitsToBytes(Ljava/nio/IntBuffer;Lnet/minecraft/client/gui/font/providers/UnihexProvider$LineData;II)V",
        at = @At("RETURN")
    )
    private static void fonttweaks$pixelateGlyph(
            IntBuffer pixels, @Coerce Object lineData, int left, int right,
            CallbackInfo ci) {
        FontTweaksSettings settings = FontTweaksSettings.get();
        if (!settings.pixelGlyphEnabled) return;

        final int width = right - left;
        PixelGlyphProcessor.pixelate(pixels, width, settings.pixelGlyphBlockSize);
    }
}
