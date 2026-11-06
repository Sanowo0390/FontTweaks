package dev.sanowo.fonttweaks.mixin;

import com.mojang.blaze3d.font.UnbakedGlyph;
import dev.sanowo.fonttweaks.CharacterGlyph;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UnihexProvider.class)
public abstract class UnihexProviderMixin {
    @Inject(method = "getGlyph", at = @At("RETURN"), cancellable = true)
    private void fonttweaks$excludeCharacter(int codepoint, CallbackInfoReturnable<UnbakedGlyph> cir) {
        UnbakedGlyph original = cir.getReturnValue();
        if (original == null) return;
        cir.setReturnValue(new CharacterGlyph(original, codepoint));
    }
}
