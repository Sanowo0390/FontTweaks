package dev.sanowo.fonttweaks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Coalesce edits, but never lose a request made during an in-flight reload. */
public final class ResourceReloadQueue {
    private long requested;
    private long attempted;
    private long due;
    private boolean running;

    public synchronized void request(long now, long delay) {
        requested++;
        due = now + delay;
    }
    public synchronized void flush() { due = Long.MIN_VALUE; }

    public synchronized void tick(long now, Supplier<CompletableFuture<Void>> reload,
                                  Runnable success, Consumer<Throwable> failure) {
        if (running || requested == attempted || now < due) return;
        running = true;
        long generation = requested;
        try {
            reload.get().whenComplete((unused, error) -> {
                synchronized (this) {
                    attempted = generation;
                    running = false;
                }
                if (error == null) success.run(); else failure.accept(error);
            });
        } catch (Throwable error) {
            attempted = generation;
            running = false;
            failure.accept(error);
        }
    }
}
