package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.sanowo.fonttweaks.FontTweaksRenderContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;

/** Controls only button labels; the widget, mouse target and its sprite stay unchanged. */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {
    @WrapMethod(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
    private void fonttweaks$widgetTextContext(GuiGraphicsExtractor graphics, int mouseX,
                                              int mouseY, float partialTick, Operation<Void> original) {
        FontTweaksRenderContext.Kind current = FontTweaksRenderContext.current();
        FontTweaksRenderContext.Kind kind =
                ((Object) this instanceof AbstractButton)
                        ? FontTweaksRenderContext.Kind.BUTTON : current;
        FontTweaksRenderContext.Kind previous = FontTweaksRenderContext.push(kind);
        try {
            original.call(graphics, mouseX, mouseY, partialTick);
        } finally {
            FontTweaksRenderContext.pop(previous);
        }
    }
}
