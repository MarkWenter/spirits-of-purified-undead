package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Five-stack melee combo. The hit after reaching five stacks consumes them and requests a follow-up. */
public final class UlvComboState {
    public static final int MAX_LAYERS = 5;
    public static final long WINDOW_TICKS = 100L;
    public static final double BONUS_PER_LAYER = 0.10D;

    private int layers;
    private long lastHitTick = Long.MIN_VALUE;
    private long lastAcceptedAttackTick = Long.MIN_VALUE;

    public enum Result {
        STACKED,
        FOLLOW_UP,
        SAME_ATTACK_IGNORED
    }

    public int layers(long now) {
        expire(now);
        return layers;
    }

    public double damageBonus(long now) {
        return layers(now) * PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvBonusPerLayer);
    }

    public Result onQualifyingHit(long now) {
        expire(now);
        if (lastAcceptedAttackTick == now) {
            return Result.SAME_ATTACK_IGNORED;
        }
        lastAcceptedAttackTick = now;
        if (layers >= PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvMaxLayers)) {
            layers = 0;
            lastHitTick = Long.MIN_VALUE;
            return Result.FOLLOW_UP;
        }
        layers++;
        lastHitTick = now;
        return Result.STACKED;
    }

    public boolean expire(long now) {
        if (layers > 0
                && (now < lastHitTick
                        || now - lastHitTick
                                > PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.ulvComboWindowTicks))) {
            layers = 0;
            lastHitTick = Long.MIN_VALUE;
            return true;
        }
        return false;
    }
}
