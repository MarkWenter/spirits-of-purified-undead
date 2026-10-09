package dev.purifiedundead.entity;

/** Preserve injuries through conversion/old-save upgrades instead of silently healing them. */
public final class BlightedHealth {
    private BlightedHealth() {}

    public static float rescale(float current, float previousMaximum, float newMaximum) {
        if (!Float.isFinite(current)
                || !Float.isFinite(previousMaximum)
                || previousMaximum <= 0
                || !Float.isFinite(newMaximum)
                || newMaximum <= 0) return 0;
        return Math.max(0, Math.min(1, current / previousMaximum)) * newMaximum;
    }
}
