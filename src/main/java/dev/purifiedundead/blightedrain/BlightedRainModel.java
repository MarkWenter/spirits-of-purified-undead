package dev.purifiedundead.blightedrain;

/** World-wide timing. No player-count multiplier and no random draws while occupied/cooling. */
public final class BlightedRainModel {
    public static final int WARNING_TICKS = 300, COOLDOWN_TICKS = 18000;
    public static final int MIN_DURATION = 6000, MAX_DURATION = 12000, DAMAGE_INTERVAL = 25;
    public static final double CHANCE_PER_SECOND = .001;

    public enum Phase {
        READY,
        WARNING,
        ACTIVE,
        COOLDOWN
    }

    private Phase phase = Phase.READY;
    private long deadline;
    private int duration = MIN_DURATION;

    public Phase phase() {
        return phase;
    }

    public long deadline() {
        return deadline;
    }

    public int duration() {
        return duration;
    }

    public int remaining(long now) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, deadline - now));
    }

    public boolean occupied() {
        return phase == Phase.WARNING || phase == Phase.ACTIVE;
    }

    public boolean canRoll(boolean rainy, boolean inhabited, long now) {
        return phase == Phase.READY && rainy && inhabited && now % 20 == 0;
    }

    public void start(long now, int ticks) {
        if (phase != Phase.READY) throw new IllegalStateException("Rain event is not ready");
        if (ticks < 20 || ticks > MAX_DURATION)
            throw new IllegalArgumentException("Duration out of bounds");
        duration = ticks;
        phase = Phase.WARNING;
        deadline = now + WARNING_TICKS;
    }

    public boolean advance(long now) {
        if (phase == Phase.READY || now < deadline) return false;
        if (phase == Phase.WARNING) {
            phase = Phase.ACTIVE;
            deadline = now + duration;
        } else if (phase == Phase.ACTIVE) {
            phase = Phase.COOLDOWN;
            deadline = now + COOLDOWN_TICKS;
        } else {
            phase = Phase.READY;
            deadline = 0;
        }
        return true;
    }

    public void stop(long now) {
        if (occupied()) {
            phase = Phase.COOLDOWN;
            deadline = now + COOLDOWN_TICKS;
        }
    }

    public void reset() {
        phase = Phase.READY;
        deadline = 0;
    }

    public void restore(int ordinal, long end, int ticks, long now) {
        phase =
                ordinal >= 0 && ordinal < Phase.values().length
                        ? Phase.values()[ordinal]
                        : Phase.READY;
        duration = Math.max(20, Math.min(MAX_DURATION, ticks));
        long limit =
                phase == Phase.WARNING
                        ? WARNING_TICKS
                        : phase == Phase.ACTIVE ? MAX_DURATION : COOLDOWN_TICKS;
        deadline = phase == Phase.READY ? 0 : Math.max(now, Math.min(end, now + limit));
    }

    public static int progress(int previous, boolean exposed, long missedTicks) {
        int value = (int) Math.max(0, Math.min(100, previous) - Math.max(0, missedTicks));
        return exposed ? Math.min(100, value + 1) : Math.max(0, value - 1);
    }
}
