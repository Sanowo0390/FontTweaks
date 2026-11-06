package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import dev.sanowo.fonttweaks.FontTweaksFontScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Only adjust wrapped line pitch here. Font glyph transforms are handled
 * centrally by GuiTextRenderStateMixin, so text is scaled exactly once.
 */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsExtractorMixin {
    @WrapMethod(method = "textWithWordWrap(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/FormattedText;IIIIZ)I")
    private int fonttweaks$wrappedText(Font font, FormattedText text, int x, int y,
                                      int width, int color, boolean shadow, Operation<Integer> original) {
        FontTweaksSettings settings = FontTweaksSettings.get();
        double spacing = settings.wrappedLineSpacing;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gui != null && mc.gui.screen() instanceof FontTweaksFontScreen) {
            return original.call(font, text, x, y, width, color, shadow);
        }
        float scale = mc == null || mc.gui == null ? 1.0f
                : settings.scaleFor(mc.gui.screen() != null);
        if (Math.abs(spacing - 1.0) < 0.000001 && Math.abs(scale - 1.0f) < 0.000001) {
            return original.call(font, text, x, y, width, color, shadow);
        }

        int wrapWidth = Math.max(1, Math.round(width / scale));
        int lineStep = Math.max(1, (int) Math.round(font.lineHeight * spacing * scale));
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
        int nextY = y;
        for (FormattedCharSequence line : font.split(text, wrapWidth)) {
            graphics.text(font, line, x, nextY, color, shadow);
            nextY += lineStep;
        }
        return nextY;
    }
}
