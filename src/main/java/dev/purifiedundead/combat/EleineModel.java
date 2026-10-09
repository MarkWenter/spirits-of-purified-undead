package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure arithmetic and provisional movement constants for the Dark Witch's blight. */
public final class EleineModel {
    public static final float MAGIC_MULTIPLIER = 1.30F;
    public static final double UNREVERSED_SWIM_SPEED_MULTIPLIER = 0.65D;
    public static final float ORB_CHANCE = 0.30F;
    public static final float ORB_DAMAGE_RATIO = 0.45F;
    public static final int WATER_BREATHING_TICKS = 200;

    private EleineModel() {}

    public static float incomingMagicDamage(
            float amount, boolean contractEquipped, boolean eleineEquipped) {
        validateDamage(amount);
        return contractEquipped && !eleineEquipped
                ? (float)
                        (amount
                                * PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.eleineMagicMultiplier))
                : amount;
    }

    public static float outgoingMagicDamage(
            float amount, boolean contractEquipped, boolean eleineEquipped) {
        validateDamage(amount);
        return contractEquipped && eleineEquipped
                ? (float)
                        (amount
                                * PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.eleineMagicMultiplier))
                : amount;
    }

    public static double swimSpeedMultiplier(boolean contractEquipped, boolean eleineEquipped) {
        return contractEquipped && !eleineEquipped
                ? PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineSwimSpeedMultiplier)
                : 1.0D;
    }

    public static float orbDamage(float triggeringPreDefenseDamage) {
        validateDamage(triggeringPreDefenseDamage);
        return (float)
                (triggeringPreDefenseDamage
                        * PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.eleineOrbDamageRatio));
    }

    public static boolean rollOrb(float randomUnitValue) {
        if (!Float.isFinite(randomUnitValue) || randomUnitValue < 0.0F || randomUnitValue >= 1.0F) {
            throw new IllegalArgumentException("Random value must be finite and in [0, 1)");
        }
        return randomUnitValue
                < PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineOrbChance);
    }

    private static void validateDamage(float amount) {
        if (!Float.isFinite(amount) || amount < 0.0F) {
            throw new IllegalArgumentException("Damage must be finite and non-negative");
        }
    }
}
