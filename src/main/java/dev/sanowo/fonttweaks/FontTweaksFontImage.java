package dev.sanowo.fonttweaks;

import dev.isxander.yacl3.gui.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Lightweight live font sample in YACL's native description image panel.
 * Follows the ImageRenderer contract exposed by Zoomify 2.17.0's demos;
 * no Zoomify runtime classes or assets are used.
 */
public final class FontTweaksFontImage implements ImageRenderer {
    @Override
    public int render(GuiGraphicsExtractor graphics, int x, int y, int width, float delta) {
        int height = 83;
        if (width < 50) return 0;

        graphics.fill(x, y, x + width, y + height, 0xA8171B25);
        graphics.fill(x, y, x + width, y + 1, 0xFF6A778D);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF6A778D);
        graphics.fill(x, y, x + 1, y + height, 0xFF6A778D);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF6A778D);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null) return height;

        FontTweaksSettings settings = FontTweaksSettings.get();
        FontTweaksRenderContext.Kind old =
                FontTweaksRenderContext.push(FontTweaksRenderContext.Kind.PREVIEW);
        try {
            int tx = x + 7;
            int maxTextWidth = width - 14;
            if (maxTextWidth > 90) {
                graphics.text(minecraft.font, Component.literal("日本語 ドット文字"), tx, y + 12,
                        0xFFFFFFFF);
                graphics.text(minecraft.font, Component.literal("ABC 012345  Aa"),
                        tx, y + 30, 0xFFFFDB8A);
                graphics.text(minecraft.font, Component.literal("漢字 かな カナ"),
                        tx, y + 48, 0xFF8DE3FF);
            } else {
                graphics.text(minecraft.font, Component.literal("日本語"), tx, y + 12, 0xFFFFFFFF);
                graphics.text(minecraft.font, Component.literal("ABC 123"), tx, y + 30, 0xFFFFDB8A);
            }
        } finally {
            FontTweaksRenderContext.pop(old);
        }

        int gui = minecraft.getWindow().getGuiScale();
        int processedPixels = settings.pixelRasterSignature(gui);
        GuiScaleProfile profile = settings.profileFor(gui);
        String pixelStatus = processedPixels == 0 ? "Native" : processedPixels + "px";
        if (profile.pixelBlockSize == FontTweaksSettings.AUTO_PIXEL_SIZE && processedPixels != 0) {
            pixelStatus = "Auto " + pixelStatus;
        }
        if (profile.useExistingFont) {
            pixelStatus += profile.effectivePixels() == 3
                    || "misaki".equals(profile.legacyExistingFont) ? " / Misaki" : " / PixelMplus";
        }
        graphics.text(minecraft.font,
                Component.literal("GUI " + gui + " / " + pixelStatus),
                x + 7, y + height - 15, 0xFFA5AFC3);
        return height;
    }

    @Override public void tick() {}
    @Override public void close() {}
}
