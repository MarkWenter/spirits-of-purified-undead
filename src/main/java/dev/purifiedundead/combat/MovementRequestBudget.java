package dev.purifiedundead.combat;

/** Server-thread only: bound correction packets and equipment scans from repeated input. */
final class MovementRequestBudget {
    private int tick;
    private int used;

    boolean allow(int serverTick) {
        if (tick != serverTick) {
            tick = serverTick;
            used = 0;
        }
        if (used >= 8) return false;
        used++;
        return true;
    }
}
