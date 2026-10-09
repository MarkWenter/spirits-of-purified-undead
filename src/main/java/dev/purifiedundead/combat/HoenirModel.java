package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure values for the Abyss Guardian's blight and Hoenir's reversal. */
public final class HoenirModel {
    public static final int REQUIRED_NEGATIVE_EFFECTS = 3;
    public static final int MARK_DURATION_TICKS = 200;
    public static final float MAX_HEALTH_REGEN_RATIO = 0.05F;
    public static final float FERIN_MARK_MULTIPLIER = 1.50F;

    private HoenirModel() {}

    public static EffectAdjustment adjustNegativeEffect(
            int amplifier, int duration, boolean contractEquipped, boolean hoenirEquipped) {
        if (amplifier < 0 || duration < -1) {
            throw new IllegalArgumentException("Invalid effect amplifier or duration");
        }
        if (!contractEquipped) {
            return new EffectAdjustment(amplifier, duration);
        }
        if (!hoenirEquipped) {
            return new EffectAdjustment(
                    Math.min(
                            255,
                            amplifier
                                    + PurifiedUndeadConfig.get(
                                            PurifiedUndeadConfig.VALUES
                                                    .hoenirUnreversedAmplifierIncrease)),
                    duration);
        }
        int adjustedDuration =
                duration == -1
                        ? -1
                        : Math.max(
                                1,
                                (int)
                                        Math.ceil(
                                                duration
                                                        * PurifiedUndeadConfig.get(
                                                                PurifiedUndeadConfig.VALUES
                                                                        .hoenirHarmfulDurationMultiplier)));
        return new EffectAdjustment(amplifier, adjustedDuration);
    }

    public static float regenerationAmount(float maximumHealth) {
        if (!Float.isFinite(maximumHealth) || maximumHealth < 0.0F) {
            throw new IllegalArgumentException("Maximum health must be finite and non-negative");
        }
        return (float)
                (maximumHealth
                        * PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.hoenirRegenerationRatio));
    }

    public static float ferinDamage(float amount, boolean ownerMarkPresent) {
        if (!Float.isFinite(amount) || amount < 0.0F) {
            throw new IllegalArgumentException("Damage must be finite and non-negative");
        }
        return ownerMarkPresent
                ? (float)
                        (amount
                                * PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.hoenirFerinMarkMultiplier))
                : amount;
    }

    public record EffectAdjustment(int amplifier, int duration) {}
}
