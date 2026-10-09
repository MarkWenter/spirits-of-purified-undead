package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Confirmed armor transformations for the Guardians' blight. */
public final class GuardianModel {
    private GuardianModel() {}

    public static double armor(double original, boolean reversed) {
        if (!Double.isFinite(original) || original < 0.0D) {
            throw new IllegalArgumentException("Original armor must be finite and non-negative");
        }
        double flat = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianArmorFlatChange);
        double scale = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianArmorScale);
        return reversed
                ? (original + flat) * (1.0D + scale)
                : Math.max(0.0D, (original - flat) * (1.0D - scale));
    }

    public static double toughness(double original, boolean reversed) {
        if (!Double.isFinite(original) || original < 0.0D) {
            throw new IllegalArgumentException(
                    "Original toughness must be finite and non-negative");
        }
        double scale = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianArmorScale);
        return Math.max(0.0D, original * (reversed ? 1.0D + scale : 1.0D - scale));
    }
}
