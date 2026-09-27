package dev.purifiedundead.content.relic;

/** Pure arithmetic shared by the server effects and their regression tests. */
public final class RelicRules {
    private RelicRules() {}

    public static double damageMultiplier(int fingers, int claws, boolean projectile, double scale) {
        return (1 + .25 * fingers * scale) * (1 + (projectile ? 1.35 * claws * scale : 0));
    }

    public static float killHealing(float health, float maximum, int necklaces, double scale) {
        return (float) (Math.max(0, maximum - health) * Math.min(1, .35 * necklaces * scale));
    }

    public static XpGain experience(int original, double remainder, int ribbons, double scale) {
        if (original <= 0 || ribbons == 0) return new XpGain(original, remainder);
        double bonus = original * .25 * ribbons * scale + remainder;
        long whole = (long) Math.floor(bonus);
        return new XpGain((int) Math.min(Integer.MAX_VALUE, original + whole), bonus - whole);
    }

    public static double healthBonus(int rosaries, int badges, double scale) {
        return (.05 * rosaries + .25 * badges) * scale;
    }

    public static boolean ready(long now, long availableAt) { return now >= availableAt; }
    public record XpGain(int amount, double remainder) {}
}
