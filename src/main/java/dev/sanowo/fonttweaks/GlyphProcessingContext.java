package dev.sanowo.fonttweaks;

/** Bake-time context: a blacklisted glyph must never enter a processed atlas. */
public final class GlyphProcessingContext {
    private static final ThreadLocal<Boolean> EXCLUDED = ThreadLocal.withInitial(() -> false);
    private GlyphProcessingContext() {}
    public static boolean excluded() { return EXCLUDED.get(); }
    public static boolean push(boolean excluded) {
        boolean old = EXCLUDED.get();
        EXCLUDED.set(excluded);
        return old;
    }
    public static void pop(boolean old) { EXCLUDED.set(old); }
}
