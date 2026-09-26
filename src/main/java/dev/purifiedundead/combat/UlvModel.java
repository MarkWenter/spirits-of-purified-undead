package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure arithmetic for the Mad Knight's blight and Ulv's reversal. */
public final class UlvModel {
    public static final double ATTACK_SPEED_PENALTY = 0.80D;
    public static final double ATTACK_SPEED_BONUS = 1.20D;
    public static final float BACKLASH_MAX_HEALTH_RATIO = 0.05F;
    public static final float FOLLOW_UP_RATIO = 0.50F;

    private UlvModel() {
    }

    public static double attackSpeedMultiplier(boolean contractEquipped, boolean ulvEquipped) {
        if (!contractEquipped) {
            return 1.0D;
        }
        return ulvEquipped ? PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvAttackSpeedBonusMultiplier)
                : PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvAttackSpeedPenaltyMultiplier);
    }

    public static float backlashDamage(float maxHealth) {
        if (!Float.isFinite(maxHealth) || maxHealth < 0.0F) {
            throw new IllegalArgumentException("Maximum health must be finite and non-negative");
        }
        return (float) (maxHealth * PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvBacklashMaxHealthRatio));
    }

    public static float followUpDamage(float qualifyingPreDefenseDamage) {
        if (!Float.isFinite(qualifyingPreDefenseDamage) || qualifyingPreDefenseDamage < 0.0F) {
            throw new IllegalArgumentException("Qualifying damage must be finite and non-negative");
        }
        return (float) (qualifyingPreDefenseDamage * PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvFollowUpDamageRatio));
    }
}
