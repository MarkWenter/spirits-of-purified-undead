package dev.purifiedundead.combat;

/** One explicit continuation per stage. Transient: never replay old input after login. */
public final class FerinContinuationBuffer {
    private int stage;
    private long startedAt;

    public boolean pending(int currentStage, long currentStart) {
        return stage == currentStage && startedAt == currentStart && stage > 0;
    }

    public void request(int currentStage, long currentStart) {
        stage = currentStage;
        startedAt = currentStart;
    }

    public boolean consume(
            int currentStage, long currentStart, long now, int lock, boolean active) {
        if (!active || stage != currentStage || startedAt != currentStart) {
            clear();
            return false;
        }
        if (stage == 0 || now - currentStart < lock) return false;
        clear();
        return true;
    }

    public void clear() {
        stage = 0;
        startedAt = 0;
    }
}
