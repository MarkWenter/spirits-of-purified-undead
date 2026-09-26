package dev.purifiedundead.combat;

import java.util.function.DoubleSupplier;

/** One critical roll per stage, shared by every target hit by that stage. */
public final class FerinCriticalModel {
    private FerinCriticalModel() { }

    public static Result roll(double chance, double criticalDamage, DoubleSupplier random) {
        if (!Double.isFinite(chance) || !Double.isFinite(criticalDamage) || chance < 0.0 || criticalDamage < 1.0) {
            throw new IllegalArgumentException("Invalid Ferin critical attributes");
        }
        float multiplier = 1.0F;
        double remainingChance = chance;
        double currentDamage = criticalDamage;
        while (random.getAsDouble() <= remainingChance && currentDamage > 1.0) {
            remainingChance -= 1.0;
            multiplier *= (float) currentDamage;
            currentDamage *= 0.85;
        }
        return new Result(multiplier, multiplier > 1.0F);
    }

    public record Result(float multiplier, boolean critical) { }
}
