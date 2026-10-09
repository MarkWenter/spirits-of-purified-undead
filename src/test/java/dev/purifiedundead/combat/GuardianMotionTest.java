package dev.purifiedundead.combat;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuardianMotionTest {
    @Test
    void jumpingReplacesDescentAndPreservesHorizontalMomentum() {
        var motion = GuardianMotion.jump(new Vec3(0.3, -1.4, -0.2), 0.52);
        assertEquals(0.52, motion.y);
        assertEquals(0.3, motion.x);
        assertEquals(-0.2, motion.z);
    }

    @Test
    void dashHasConstantHorizontalSpeedAndDoesNotCancelAscent() {
        for (int yaw = 0; yaw < 360; yaw += 15) {
            var motion = GuardianMotion.dash(new Vec3(0, 0.4, 0), yaw, 1.15);
            assertEquals(1.15, Math.sqrt(motion.x * motion.x + motion.z * motion.z), 1e-9);
            assertEquals(0.4, motion.y);
        }
        assertEquals(0.08, GuardianMotion.dash(new Vec3(0, -2, 0), 0, 1.15).y);
    }

    @Test
    void queuedActionsExpireAndAreConsumedOnlyOnce() {
        var state = new GuardianMovementService.AirState();
        state.queueForTakeoff(
                dev.purifiedundead.network.GuardianActionPacket.Action.DOUBLE_JUMP, 10);
        assertEquals(1, state.takePending(12).size());
        assertTrue(state.takePending(12).isEmpty());
        state.queueForTakeoff(dev.purifiedundead.network.GuardianActionPacket.Action.AIR_DASH, 20);
        assertTrue(state.takePending(26).isEmpty());
    }

    @Test
    void malformedSettingsCannotEnterClientPrediction() {
        assertTrue(GuardianMotion.validSettings(0, 10));
        assertTrue(GuardianMotion.validSettings(.52, 1.15));
        for (double invalid :
                new double[] {
                    Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -.01, 10.01
                }) {
            assertFalse(GuardianMotion.validSettings(invalid, 1.15));
            assertFalse(GuardianMotion.validSettings(.52, invalid));
        }
    }

    @Test
    void movementBurstIsBoundedAndRecoversOnNextTick() {
        var budget = new MovementRequestBudget();
        for (int i = 0; i < 8; i++) assertTrue(budget.allow(10));
        for (int i = 0; i < 10000; i++) assertFalse(budget.allow(10));
        assertTrue(budget.allow(11));
        assertTrue(budget.allow(Integer.MIN_VALUE));
    }
}
