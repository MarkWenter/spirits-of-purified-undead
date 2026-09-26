package dev.purifiedundead.combat;

import dev.purifiedundead.config.PurifiedUndeadConfig;

/** Pure numeric and timing rules for the Elder Warrior's blight. */
public final class GrothModel {
    public static final int STUN_DURATION_TICKS = 60;
    public static final int STUN_COOLDOWN_TICKS = 200;

    private GrothModel() {
    }

    public static float incomingDamageMultiplier(boolean contract, boolean grothEquipped, boolean onGround) {
        if (!contract) {
            return 1.0F;
        }
        if (!grothEquipped) {
            return PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothUnreversedDamageMultiplier).floatValue();
        }
        return onGround ? PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothGroundDamageMultiplier).floatValue() : 1.0F;
    }

    public static float miningSpeedMultiplier(boolean contract, boolean grothEquipped) {
        return contract && !grothEquipped
                ? PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothMiningSpeedMultiplier).floatValue() : 1.0F;
    }

    public static boolean canTriggerStun(boolean reversed, boolean jumpAttack, long gameTick, long cooldownUntil) {
        return reversed && jumpAttack && gameTick >= cooldownUntil;
    }
}
