package dev.purifiedundead.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardianInputModelTest {
    @Test
    void initialHeldJumpCannotBecomeDoubleJump() {
        assertFalse(GuardianInputModel.shouldDoubleJump(true, false, false, false));
        assertFalse(GuardianInputModel.shouldDoubleJump(true, true, false, false));
    }

    @Test
    void onlyASecondPressAfterAirborneReleaseTriggers() {
        assertFalse(GuardianInputModel.shouldDoubleJump(false, true, true, false));
        assertTrue(GuardianInputModel.shouldDoubleJump(true, false, true, false));
        assertFalse(GuardianInputModel.shouldDoubleJump(true, false, true, true));
    }
}
