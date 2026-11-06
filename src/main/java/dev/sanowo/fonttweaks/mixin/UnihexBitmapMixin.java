package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksSettings;
import dev.sanowo.fonttweaks.UnifontPixelation;
import java.nio.IntBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Only Unihex bitmap uploads are modified; advances, bearings and oversampling stay vanilla. */
@Mixin(targets = "net.minecraft.client.gui.font.providers.UnihexProvider$Glyph$2")
public abstract class UnihexBitmapMixin {
    @ModifyArg(method = "upload", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/system/MemoryUtil;memByteBuffer(Ljava/nio/IntBuffer;)Ljava/nio/ByteBuffer;"), index = 0)
    private IntBuffer fonttweaks$pixelate(IntBuffer pixels) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        return cfg.pixelationEnabled
                ? UnifontPixelation.apply(pixels, pixels.remaining() / 16, 16, cfg.pixelBlockSize)
                : pixels;
    }
}
