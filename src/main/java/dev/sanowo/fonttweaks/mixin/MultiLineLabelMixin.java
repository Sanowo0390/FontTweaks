package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.components.MultiLineLabel$2")
public abstract class MultiLineLabelMixin {
    @ModifyVariable(method = "visitLines", at = @At("HEAD"), argsOnly = true, index = 4)
    private int fonttweaks$lineStep(int original) {
        return FontTweaksSettings.get().lineStep(original);
    }
}
