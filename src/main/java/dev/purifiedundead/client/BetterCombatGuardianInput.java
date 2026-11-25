package dev.purifiedundead.client;

import net.bettercombat.api.client.BetterCombatClientEvents;

/** Better Combat consumes the vanilla attack input when it starts a weapon's upswing. */
final class BetterCombatGuardianInput {
    static void register() {
        BetterCombatClientEvents.ATTACK_START.register((player, hand) -> {
            if (!hand.isOffHand()) GuardianWaveInput.requestSwing();
        });
    }

    private BetterCombatGuardianInput() {}
}
