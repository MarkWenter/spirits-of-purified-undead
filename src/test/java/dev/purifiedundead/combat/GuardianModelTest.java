package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.purifiedundead.network.GuardianActionPacket;

class GuardianModelTest {
    @Test
    void armorUsesConfirmedAddThenMultiplyOrder() {
        assertEquals(13.5D, GuardianModel.armor(20.0D, false), 1.0E-9D);
        assertEquals(27.5D, GuardianModel.armor(20.0D, true), 1.0E-9D);
        assertEquals(0.0D, GuardianModel.armor(4.0D, false));
    }

    @Test
    void toughnessScalesAndNeverAcceptsInvalidInput() {
        assertEquals(7.2D, GuardianModel.toughness(8.0D, false));
        assertEquals(8.8D, GuardianModel.toughness(8.0D, true), 1.0E-9D);
        assertThrows(IllegalArgumentException.class, () -> GuardianModel.armor(-1.0D, false));
        assertThrows(
                IllegalArgumentException.class, () -> GuardianModel.toughness(Double.NaN, true));
    }

    @Test
    void takeoffBufferKeepsBothActionsAndExpiresAfterGraceWindow() {
        GuardianMovementService.AirState state = new GuardianMovementService.AirState();
        state.queueForTakeoff(GuardianActionPacket.Action.DOUBLE_JUMP, 100L);
        state.queueForTakeoff(GuardianActionPacket.Action.AIR_DASH, 101L);
        assertTrue(state.hasLivePending(106L));
        var actions = state.takePending(106L);
        assertTrue(actions.contains(GuardianActionPacket.Action.DOUBLE_JUMP));
        assertTrue(actions.contains(GuardianActionPacket.Action.AIR_DASH));
        assertFalse(state.hasLivePending(106L));

        state.queueForTakeoff(GuardianActionPacket.Action.DOUBLE_JUMP, 200L);
        assertFalse(state.hasLivePending(206L));
    }
}
