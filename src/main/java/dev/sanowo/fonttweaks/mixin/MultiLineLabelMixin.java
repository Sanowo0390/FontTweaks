package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import dev.sanowo.fonttweaks.FontTweaksSpacingPolicy;
import dev.sanowo.fonttweaks.FontTweaksUiContext;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * MultiLineLabel is the widget rendering path used by many descriptions.
 * For English exclusion, apply different pitch per rendered line rather
 * than changing only the container's calculated height.
 */
@Mixin(targets = "net.minecraft.client.gui.components.MultiLineLabel$2")
public abstract class MultiLineLabelMixin {
    /** [vanillaStep, cumulativeExtraY]. Scoped to nested visitLines calls. */
    @Unique private static final ThreadLocal<int[]> fonttweaks$lineFrame = new ThreadLocal<>();

    @ModifyVariable(method = "visitLines", at = @At("HEAD"), argsOnly = true, index = 4)
    private int fonttweaks$lineStep(int original) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        // The per-line handler below handles English exclusion.
        return cfg.excludeEnglishFromSpacing || FontTweaksUiContext.isOwnSettingsScreen()
                ? original : cfg.lineStep(original);
    }

    @WrapMethod(method = "visitLines")
    private int fonttweaks$perLineSpacing(TextAlignment alignment, int x, int y,
                                          int lineHeight, ActiveTextCollector output,
                                          Operation<Integer> original) {
        FontTweaksSettings cfg = FontTweaksSettings.get();
        if (!cfg.excludeEnglishFromSpacing || FontTweaksUiContext.isOwnSettingsScreen()) {
            return original.call(alignment, x, y, lineHeight, output);
        }
        int[] old = fonttweaks$lineFrame.get();
        int[] frame = new int[]{lineHeight, 0};
        fonttweaks$lineFrame.set(frame);
        try {
            int result = original.call(alignment, x, y, lineHeight, output);
            return result + frame[1];
        } finally {
            if (old == null) fonttweaks$lineFrame.remove();
            else fonttweaks$lineFrame.set(old);
        }
    }

    @WrapOperation(method = "visitLines", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(IILnet/minecraft/util/FormattedCharSequence;)V"))
    private void fonttweaks$positionLine(ActiveTextCollector output, int x, int y,
                                         FormattedCharSequence line, Operation<Void> original) {
        int[] frame = fonttweaks$lineFrame.get();
        if (frame == null) {
            original.call(output, x, y, line);
            return;
        }
        original.call(output, x, y + frame[1], line);
        FontTweaksSettings cfg = FontTweaksSettings.get();
        frame[1] += FontTweaksSpacingPolicy.lineStep(
                frame[0], cfg.globalLineSpacing, line, true) - frame[0];
    }
}

