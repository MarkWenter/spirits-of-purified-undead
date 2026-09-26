package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure combat arithmetic for Ferin's blight and its warrior reversal. */
public final class FerinBlightModel {
    public static final float UNREVERSED_MULTIPLIER = 0.70F;
    public static final float REVERSED_MULTIPLIER = 1.30F;
    public static final float LIFESTEAL_RATIO = 0.20F;

    private FerinBlightModel() {
    }

    public static float meleeDamage(float amount, boolean contractEquipped, boolean ferinEquipped) {
        return meleeDamage(amount, contractEquipped, ferinEquipped, 0.0D);
    }

    public static float meleeDamage(float amount, boolean contractEquipped, boolean ferinEquipped,
                                    double sameLayerBonus) {
        if (!Float.isFinite(amount) || amount < 0.0F) {
            throw new IllegalArgumentException("Melee damage must be finite and non-negative");
        }
        if (!Double.isFinite(sameLayerBonus) || sameLayerBonus < 0.0D) {
            throw new IllegalArgumentException("Same-layer bonus must be finite and non-negative");
        }
        if (!contractEquipped && sameLayerBonus == 0.0D) {
            return amount;
        }
        double contractMultiplier = contractEquipped
                ? (ferinEquipped ? PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinReversedMeleeMultiplier)
                : PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinUnreversedMeleeMultiplier)) : 1.0D;
        return (float) (amount * (contractMultiplier + sameLayerBonus));
    }

    public static float lifesteal(float finalDamage, float victimHealth) {
        if (!Float.isFinite(finalDamage) || !Float.isFinite(victimHealth)
                || finalDamage <= 0.0F || victimHealth <= 0.0F) {
            return 0.0F;
        }
        return (float) (Math.min(finalDamage, victimHealth)
                * PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinLifestealRatio));
    }
}
