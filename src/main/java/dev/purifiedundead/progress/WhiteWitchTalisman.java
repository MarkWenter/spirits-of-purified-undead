package dev.purifiedundead.progress;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure progression rules for the contract's built-in White Witch talisman. */
public final class WhiteWitchTalisman {
    public static final int MAX_LEVEL = 15;
    public static final int SPIRITS_PER_LEVEL = 4;

    private WhiteWitchTalisman() {}

    public static int clampLevel(int level) {
        return Math.max(0, Math.min(maxLevel(), level));
    }

    public static float incomingDamageMultiplier(int level) {
        return Math.max(
                0.0F,
                (float)
                        (PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.talismanBaseIncomingMultiplier)
                                - PurifiedUndeadConfig.get(
                                                PurifiedUndeadConfig.VALUES
                                                        .talismanReductionPerLevel)
                                        * clampLevel(level)));
    }

    public static int ferinMaxStages(int level) {
        int safeLevel = clampLevel(level);
        return safeLevel
                        >= PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.ferinFifthStageLevel)
                ? 5
                : safeLevel
                                >= PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.ferinFourthStageLevel)
                        ? 4
                        : 3;
    }

    public static int maxLevel() {
        return PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.talismanMaxLevel);
    }

    public static int spiritsPerLevel() {
        return PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.spiritsPerTalismanLevel);
    }
}
