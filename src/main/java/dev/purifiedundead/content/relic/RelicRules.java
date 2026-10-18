package dev.purifiedundead.content.relic;

/** Pure arithmetic shared by the server effects and their regression tests. */
public final class RelicRules {
    private static double value(net.minecraftforge.common.ForgeConfigSpec.DoubleValue v) {
        return dev.purifiedundead.config.PurifiedUndeadConfig.get(v);
    }

    private RelicRules() {}

    public static double damageMultiplier(
            int fingers, int claws, boolean projectile, double scale) {
        return (1
                        + value(
                                        dev.purifiedundead.config.PurifiedUndeadConfig.VALUES
                                                .relicFingerDamage)
                                * fingers
                                * scale)
                * (1
                        + (projectile
                                ? value(
                                                dev.purifiedundead.config.PurifiedUndeadConfig
                                                        .VALUES
                                                        .relicClawDamage)
                                        * claws
                                        * scale
                                : 0));
    }

    public static float killHealing(float health, float maximum, int necklaces, double scale) {
        return (float)
                (Math.max(0, maximum - health)
                        * Math.min(
                                1,
                                value(
                                                dev.purifiedundead.config.PurifiedUndeadConfig
                                                        .VALUES
                                                        .relicNecklaceHealing)
                                        * necklaces
                                        * scale));
    }

    public static XpGain experience(int original, double remainder, int ribbons, double scale) {
        if (original <= 0 || ribbons == 0) return new XpGain(original, remainder);
        double bonus =
                original
                                * value(
                                        dev.purifiedundead.config.PurifiedUndeadConfig.VALUES
                                                .relicRibbonExperience)
                                * ribbons
                                * scale
                        + remainder;
        long whole = (long) Math.floor(bonus);
        return new XpGain((int) Math.min(Integer.MAX_VALUE, original + whole), bonus - whole);
    }

    public static double healthBonus(int rosaries, int badges, double scale) {
        return (value(dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.relicRosaryHealth)
                                * rosaries
                        + value(
                                        dev.purifiedundead.config.PurifiedUndeadConfig.VALUES
                                                .relicBadgeHealth)
                                * badges)
                * scale;
    }

    public static boolean ready(long now, long availableAt) {
        return now >= availableAt;
    }

    public record XpGain(int amount, double remainder) {}
}
