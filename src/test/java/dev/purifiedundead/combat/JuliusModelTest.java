package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JuliusModelTest {
    @Test
    void reachAndSprintReverseWithJulius() {
        assertEquals(0.0D, JuliusModel.attackReachDelta(false, false));
        assertEquals(0.0D, JuliusModel.attackReachDelta(true, false));
        assertEquals(1.0D, JuliusModel.attackReachDelta(true, true));
        assertEquals(1.0D, JuliusModel.sprintSpeedMultiplier(false, false));
        assertEquals(0.8D, JuliusModel.sprintSpeedMultiplier(true, false));
        assertEquals(1.2D, JuliusModel.sprintSpeedMultiplier(true, true));
    }
}
