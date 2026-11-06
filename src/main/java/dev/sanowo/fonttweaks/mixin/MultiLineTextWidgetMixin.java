package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksSettings;
import dev.sanowo.fonttweaks.FontTweaksUiContext;
import dev.sanowo.fonttweaks.FontTweaksSpacingPolicy;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MultiLineTextWidget.class)
public abstract class MultiLineTextWidgetMixin {
    @ModifyConstant(method = "getHeight", constant = @Constant(intValue = 9))
    private int fonttweaks$height(int original) {
        if (FontTweaksUiContext.isOwnSettingsScreen()) return original;
        FontTweaksSettings cfg = FontTweaksSettings.get();
        if (cfg.excludeEnglishFromSpacing && !FontTweaksSpacingPolicy.containsNonEnglish(
                ((MultiLineTextWidget) (Object) this).getMessage().getString())) return original;
        return cfg.lineStep(original);
    }
}
