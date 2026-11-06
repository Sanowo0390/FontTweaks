package dev.sanowo.fonttweaks.mixin;

import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only changes per-line advance for textual tooltips; image tooltip rows stay unchanged. */
@Mixin(ClientTextTooltip.class)
public abstract class ClientTextTooltipMixin {
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void fonttweaks$height(Font font, CallbackInfoReturnable<Integer> cir) {
        float spacing = FontTweaksSettings.get().tooltipLineSpacing;
        if (Math.abs(spacing - 1.0f) < 0.001f) return;
        cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValue() * spacing)));
    }
}
