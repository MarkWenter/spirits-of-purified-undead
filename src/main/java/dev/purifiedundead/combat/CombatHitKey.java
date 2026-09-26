package dev.purifiedundead.combat;

import java.util.UUID;

/** Identifies one attacker's pending hit against one target. */
record CombatHitKey(UUID targetId, UUID attackerId) {
    CombatHitKey {
        if (targetId == null || attackerId == null) {
            throw new IllegalArgumentException("Combat hit identities must be present");
        }
    }
}
