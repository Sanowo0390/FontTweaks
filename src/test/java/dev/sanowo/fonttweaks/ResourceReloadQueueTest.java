package dev.sanowo.fonttweaks;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceReloadQueueTest {
    @Test void aToggleStartsReloadWithoutClosingScreenAndCoalescesRequests() {
        ResourceReloadQueue queue = new ResourceReloadQueue();
        int[] starts = {0};
        queue.request(10, 0);
        queue.request(10, 0);
        queue.tick(10, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> {}, error -> fail(error));
        queue.tick(11, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> {}, error -> fail(error));
        assertEquals(1, starts[0]);
    }

    @Test void editsDuringReloadAreReplayedAfterCompletion() {
        ResourceReloadQueue queue = new ResourceReloadQueue();
        CompletableFuture<Void> first = new CompletableFuture<>();
        int[] starts = {0}, completed = {0};
        queue.request(0, 0);
        queue.tick(0, () -> { starts[0]++; return first; }, () -> completed[0]++, error -> fail(error));
        queue.request(1, 0);
        queue.tick(1, () -> { starts[0]++; return first; }, () -> completed[0]++, error -> fail(error));
        assertEquals(1, starts[0]);
        first.complete(null);
        queue.tick(2, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> completed[0]++, error -> fail(error));
        assertEquals(2, starts[0]);
        assertEquals(2, completed[0]);
    }

    @Test void typingIsDebouncedAndLeavingFlushesPendingEdits() {
        ResourceReloadQueue queue = new ResourceReloadQueue();
        int[] starts = {0};
        queue.request(-1000, 350);
        queue.request(-900, 350);
        queue.tick(-600, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> {}, error -> fail(error));
        assertEquals(0, starts[0]);
        queue.flush();
        queue.tick(-600, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> {}, error -> fail(error));
        assertEquals(1, starts[0]);
    }

    @Test void failureDoesNotLoopButNewRequestCanRetry() {
        ResourceReloadQueue queue = new ResourceReloadQueue();
        int[] errors = {0}, starts = {0};
        queue.request(0, 0);
        queue.tick(0, () -> { starts[0]++; return CompletableFuture.failedFuture(new IllegalStateException()); }, () -> fail(), error -> errors[0]++);
        queue.tick(1, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> {}, error -> fail(error));
        assertEquals(1, starts[0]);
        assertEquals(1, errors[0]);
        queue.request(2, 0);
        queue.tick(2, () -> { starts[0]++; return CompletableFuture.completedFuture(null); }, () -> {}, error -> fail(error));
        assertEquals(2, starts[0]);
    }
}
