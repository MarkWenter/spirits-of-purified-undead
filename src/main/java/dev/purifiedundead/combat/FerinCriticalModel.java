package dev.purifiedundead.combat;

import java.util.function.DoubleSupplier;

/** One critical roll per stage, shared by every target hit by that stage. */
public final class FerinCriticalModel {
    private FerinCriticalModel() {}

    public static Result roll(double chance, double criticalDamage, DoubleSupplier random) {
        if (!Double.isFinite(chance)
                || !Double.isFinite(criticalDamage)
                || chance < 0.0
                || criticalDamage < 1.0) {
            throw new IllegalArgumentException("Invalid Ferin critical attributes");
        }
        float multiplier = 1.0F;
        double remainingChance = chance;
        double currentDamage = criticalDamage;
        while (random.getAsDouble() <= remainingChance && currentDamage > 1.0) {
            remainingChance -= 1.0;
            multiplier *= (float) currentDamage;
            if (!Float.isFinite(multiplier)) return new Result(1.0F, false);
            currentDamage *= 0.85;
        }
        return new Result(multiplier, multiplier > 1.0F);
    }

    public static Result rollExternal(double chance, double damage, DoubleSupplier random) {
        if (!Double.isFinite(chance) || chance < 0 || !Double.isFinite(damage) || damage < 1) {
            dev.purifiedundead.util.FailureIsolation.report(
                    "invalid external critical attributes",
                    new IllegalArgumentException(
                            "Critical attributes must be finite and in range"));
            return new Result(1.0F, false);
        }
        return roll(chance, damage, random);
    }

    public record Result(float multiplier, boolean critical) {}
}
