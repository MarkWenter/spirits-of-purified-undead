package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure numeric rules for the Knight Captain's blight. */
public final class JuliusModel {
    private JuliusModel() {}

    public static double attackReachDelta(boolean contract, boolean juliusEquipped) {
        return contract
                ? (juliusEquipped
                        ? PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.juliusReachBonus)
                        : 0.0D)
                : 0.0D;
    }

    public static double sprintSpeedMultiplier(boolean contract, boolean juliusEquipped) {
        return contract
                ? (juliusEquipped
                        ? PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.juliusSprintBonusMultiplier)
                        : PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.juliusSprintPenaltyMultiplier))
                : 1.0D;
    }
}
