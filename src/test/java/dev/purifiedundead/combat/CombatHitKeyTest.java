package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatHitKeyTest {
    @Test
    void keepsSimultaneousAttackersOnTheSameTargetIndependent() {
        UUID target = UUID.randomUUID();
        CombatHitKey first = new CombatHitKey(target, UUID.randomUUID());
        CombatHitKey second = new CombatHitKey(target, UUID.randomUUID());
        Map<CombatHitKey, String> hits = new HashMap<>();

        hits.put(first, "ferin-owner");
        hits.put(second, "ulv-owner");

        assertEquals(2, hits.size());
        assertEquals("ferin-owner", hits.get(first));
        assertEquals("ulv-owner", hits.get(second));
    }
}
