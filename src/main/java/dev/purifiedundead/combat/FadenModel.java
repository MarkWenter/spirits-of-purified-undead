package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure constants and arithmetic for the Heretic's blight and Faden's reversal. */
public final class FadenModel {
    public static final double KNOCKBACK_RESISTANCE_PENALTY = -0.15D;
    public static final float POISON_CHANCE = 0.20F;
    public static final int POISON_DURATION_TICKS = 400;
    public static final int POISON_AMPLIFIER = 0;

    private FadenModel() {}

    public static int effectiveEnchantmentLevel(int originalLevel, boolean reversed) {
        if (originalLevel < 0) {
            throw new IllegalArgumentException("Enchantment level cannot be negative");
        }
        return reversed
                ? originalLevel
                        + PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.fadenFortuneLootingBonus)
                : originalLevel;
    }

    public static boolean rollPoison(float randomUnitValue) {
        if (!Float.isFinite(randomUnitValue) || randomUnitValue < 0.0F || randomUnitValue >= 1.0F) {
            throw new IllegalArgumentException("Random value must be finite and in [0, 1)");
        }
        return randomUnitValue
                < PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenPoisonChance);
    }
}
