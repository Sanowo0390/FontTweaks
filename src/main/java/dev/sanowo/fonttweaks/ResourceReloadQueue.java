package dev.sanowo.fonttweaks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Coalesce edits, but never lose a request made during an in-flight reload. */
public final class ResourceReloadQueue {
    private long requested;
    private long attempted;
    private boolean running;

    public synchronized void request() {
        requested++;
    }

    public synchronized void tick(Supplier<CompletableFuture<Void>> reload,
                                  Runnable success, Consumer<Throwable> failure) {
        if (running || requested == attempted) return;
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
