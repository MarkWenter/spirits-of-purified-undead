package dev.purifiedundead.util;

/** Server-thread retry schedule: immediate first attempt, then 1s to 60s after failures. */
public final class RetryBackoff {
    private int delay = 20;
    private long next;
    private boolean waiting;

    public boolean ready(long tick) {
        return !waiting || tick >= next;
    }

    public void failed(long tick) {
        next = tick + delay;
        waiting = true;
        delay = Math.min(1200, delay * 2);
    }
}
