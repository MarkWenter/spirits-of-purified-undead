package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Confirmed per-stage coefficients applied to each stage's own damage snapshot. */
public final class FerinDamageModel {
    private static final double[] COEFFICIENTS = {0.75, 0.75, 0.65, 0.65, 1.25};

    private FerinDamageModel() {}

    public static float baseDamage(int stage, float preDefenseSnapshot) {
        if (stage < 1 || stage > COEFFICIENTS.length) {
            throw new IllegalArgumentException("Ferin damage stage must be between one and five");
        }
        if (!Float.isFinite(preDefenseSnapshot) || preDefenseSnapshot < 0.0F) {
            throw new IllegalArgumentException(
                    "Ferin damage snapshot must be finite and non-negative");
        }
        java.util.List<? extends Double> configured =
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinStageDamageMultipliers);
        double coefficient =
                configured.size() == COEFFICIENTS.length
                        ? configured.get(stage - 1)
                        : COEFFICIENTS[stage - 1];
        return (float) (preDefenseSnapshot * coefficient);
    }
}
