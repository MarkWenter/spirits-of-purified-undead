package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerinMeleeTriggerTest {
    @Test
    void acceptsOnlyEquippedDirectPlayerMeleeThatLosesHealth() {
        assertTrue(FerinMeleeTrigger.qualifies(true, true, true, true, 1.0F));
        assertFalse(FerinMeleeTrigger.qualifies(false, true, true, true, 1.0F));
        assertFalse(FerinMeleeTrigger.qualifies(true, false, true, true, 1.0F));
        assertFalse(FerinMeleeTrigger.qualifies(true, true, false, true, 1.0F));
        assertFalse(FerinMeleeTrigger.qualifies(true, true, true, false, 1.0F));
        assertFalse(FerinMeleeTrigger.qualifies(true, true, true, true, 0.0F));
        assertFalse(FerinMeleeTrigger.qualifies(true, true, true, true, Float.NaN));
    }

    @Test
    void collapsesSweepVictimsWithinOneAttackTick() {
        var gate = new FerinMeleeTrigger.AttackTickGate();
        assertTrue(gate.accept(20));
        assertFalse(gate.accept(20));
        assertTrue(gate.accept(21));
    }
}
