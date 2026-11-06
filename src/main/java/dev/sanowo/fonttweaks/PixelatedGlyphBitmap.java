package dev.sanowo.fonttweaks;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryUtil;

/** More texture pixels, same quad bounds and original GlyphInfo. */
public final class PixelatedGlyphBitmap implements GlyphBitmap {
    private final GlyphBitmap original;
    private final int[] pixels;
    private final int guiScale;

    public PixelatedGlyphBitmap(GlyphBitmap original, int[] rows, int left, int guiScale, int target) {
        this.original = original;
        this.guiScale = guiScale;
        this.pixels = UnifontPixelation.rasterize(rows, left, original.getPixelWidth(), guiScale, target);
    }
    public int getPixelWidth() { return original.getPixelWidth() * guiScale; }
    public int getPixelHeight() { return original.getPixelHeight() * guiScale; }
    public float getOversample() { return original.getOversample() * guiScale; }
    public boolean isColored() { return original.isColored(); }
    public float getBearingLeft() { return original.getBearingLeft(); }
    public float getBearingTop() { return original.getBearingTop(); }
    public float getLeft() { return original.getLeft(); }
    public float getRight() { return original.getRight(); }
    public float getTop() { return original.getTop(); }
    public float getBottom() { return original.getBottom(); }

    public void upload(int x, int y, GpuTexture texture) {
        IntBuffer buffer = MemoryUtil.memAllocInt(pixels.length);
        try {
            buffer.put(pixels).flip();
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture,
                    MemoryUtil.memByteBuffer(buffer), 0, 0, x, y, getPixelWidth(), getPixelHeight());
        } finally { MemoryUtil.memFree(buffer); }
    }
}
