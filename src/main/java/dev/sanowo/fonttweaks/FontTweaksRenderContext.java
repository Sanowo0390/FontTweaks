package dev.sanowo.fonttweaks;

/**
 * Rendering context is scoped to the current call stack, not stored in a shared
 * global flag. This makes nested text render calls deterministic.
 */
public final class FontTweaksRenderContext {
    public enum Kind { DEFAULT, BUTTON, CHAT }

    private static final ThreadLocal<Kind> ACTIVE =
            ThreadLocal.withInitial(() -> Kind.DEFAULT);

    private FontTweaksRenderContext() {}

    public static Kind current() {
        return ACTIVE.get();
    }

    public static Kind push(Kind kind) {
        Kind old = ACTIVE.get();
        ACTIVE.set(kind);
        return old;
    }

    public static void pop(Kind previous) {
        ACTIVE.set(previous);
    }
}
