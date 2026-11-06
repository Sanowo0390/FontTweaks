package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MultiLineTextWidget.class)
public abstract class MultiLineTextWidgetMixin {
    @ModifyConstant(method = "getHeight", constant = @Constant(intValue = 9))
    private int fonttweaks$height(int original) {
        return FontTweaksSettings.get().lineStep(original);
    }
}
