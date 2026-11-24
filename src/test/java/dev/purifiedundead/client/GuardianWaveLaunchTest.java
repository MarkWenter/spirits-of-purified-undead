package dev.purifiedundead.client;

import static org.junit.jupiter.api.Assertions.*;
import dev.purifiedundead.entity.GuardianWaveEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GuardianWaveLaunchTest {
    private static void near(Vec3 expected, Vec3 actual) {
        assertEquals(0, expected.distanceTo(actual), 1e-5);
    }

    @Test void originFollowsTheSwordArmAndLaunchFacingIncludingVerticalShots() {
        var arm = HumanoidArm.RIGHT;
        near(new Vec3(-.36, -.16, 0), GuardianWaveRenderer.handOffset(arm, 0, 0, 0));
        near(new Vec3(.36, -.16, 0), GuardianWaveRenderer.handOffset(HumanoidArm.LEFT, 0, 0, 0));
        near(new Vec3(0, -.16, .36), GuardianWaveRenderer.handOffset(arm, -90, 0, 0));
        near(new Vec3(.36, -.16, 0), GuardianWaveRenderer.handOffset(arm, 180, 0, 0));
        near(new Vec3(-.36, 0, -.16), GuardianWaveRenderer.handOffset(arm, 0, 90, 0));
        near(new Vec3(-.36, 0, .16), GuardianWaveRenderer.handOffset(arm, 0, -90, 0));
    }

    @Test void bothHandsConvergeContinuouslyOnTheOriginalRangeEndpoint() {
        double range = GuardianWaveEntity.RANGE;
        for (var arm : HumanoidArm.values()) {
            Vec3 start = GuardianWaveRenderer.handOffset(arm, 31, -17, 0);
            for (double d : new double[]{.25, 1, 6.75, range - .1, range}) {
                near(start.scale(1 - d / range), GuardianWaveRenderer.handOffset(arm, 31, -17, d));
            }
            near(start, GuardianWaveRenderer.handOffset(arm, 31, -17, -1));
            near(Vec3.ZERO, GuardianWaveRenderer.handOffset(arm, 31, -17, range + 1));
        }
    }
}
