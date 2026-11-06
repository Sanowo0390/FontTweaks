package dev.sanowo.fonttweaks.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.sanowo.fonttweaks.FontTweaksRenderContext;
import dev.sanowo.fonttweaks.FontTweaksSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adjust the vanilla chat scale itself so that the message backgrounds,
 * line wrapping and click targeting follow the new chat text size.
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @ModifyReturnValue(method = "getScale", at = @At("RETURN"))
    private double fonttweaks$chatSize(double original) {
        FontTweaksSettings settings = FontTweaksSettings.get();
        return Math.max(0.01, original * settings.globalScale * settings.chatScale);
    }

    @WrapMethod(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V")
    private void fonttweaks$chatContext(GuiGraphicsExtractor graphics, Font font, int ticks,
                                       int mouseX, int mouseY, ChatComponent.DisplayMode mode,
                                       boolean changeCursorOnInsertions, Operation<Void> original) {
        FontTweaksRenderContext.Kind previous =
                FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.CHAT);
        try {
            original.call(graphics, font, ticks, mouseX, mouseY, mode, changeCursorOnInsertions);
        } finally {
            FontTweaksRenderContext.pop(previous);
        }
    }
}
