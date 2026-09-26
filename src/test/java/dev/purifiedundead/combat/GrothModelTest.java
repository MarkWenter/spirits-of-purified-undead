package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrothModelTest {
    @Test
    void blightAndReversalUseConfirmedMultipliers() {
        assertEquals(1.0F, GrothModel.incomingDamageMultiplier(false, false, true), 0.0001F);
        assertEquals(1.2F, GrothModel.incomingDamageMultiplier(true, false, true), 0.0001F);
        assertEquals(0.8F, GrothModel.incomingDamageMultiplier(true, true, true), 0.0001F);
        assertEquals(1.0F, GrothModel.incomingDamageMultiplier(true, true, false), 0.0001F);
        assertEquals(0.9F, GrothModel.miningSpeedMultiplier(true, false), 0.0001F);
        assertEquals(1.0F, GrothModel.miningSpeedMultiplier(true, true), 0.0001F);
    }

    @Test
    void stunRequiresReversalJumpAttackAndExpiredCooldown() {
        assertFalse(GrothModel.canTriggerStun(false, true, 200, 100));
        assertFalse(GrothModel.canTriggerStun(true, false, 200, 100));
        assertFalse(GrothModel.canTriggerStun(true, true, 199, 200));
        assertTrue(GrothModel.canTriggerStun(true, true, 200, 200));
    }
}
