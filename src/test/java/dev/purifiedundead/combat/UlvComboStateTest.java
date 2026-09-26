package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UlvComboStateTest {
    @Test
    void sixthQualifyingAttackConsumesFiveStacksAndRequestsFollowUp() {
        UlvComboState state = new UlvComboState();
        for (int hit = 1; hit <= 5; hit++) {
            assertEquals(UlvComboState.Result.STACKED, state.onQualifyingHit(hit * 10L));
            assertEquals(hit, state.layers(hit * 10L));
            assertEquals(hit * 0.10D, state.damageBonus(hit * 10L), 1.0E-9D);
        }
        assertEquals(UlvComboState.Result.FOLLOW_UP, state.onQualifyingHit(60L));
        assertEquals(0, state.layers(60L));
    }

    @Test
    void onePlayerAttackTickCannotAddMultipleStacks() {
        UlvComboState state = new UlvComboState();
        assertEquals(UlvComboState.Result.STACKED, state.onQualifyingHit(20L));
        assertEquals(UlvComboState.Result.SAME_ATTACK_IGNORED, state.onQualifyingHit(20L));
        assertEquals(1, state.layers(20L));
    }

    @Test
    void exactlyFiveSecondsContinuesButLongerGapClears() {
        UlvComboState state = new UlvComboState();
        state.onQualifyingHit(10L);
        assertEquals(1, state.layers(110L));
        assertEquals(0, state.layers(111L));
        assertEquals(UlvComboState.Result.STACKED, state.onQualifyingHit(112L));
        assertEquals(1, state.layers(112L));
    }
}
