package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerinBladeTrajectoryTest {
    @Test
    void requestedReachAndTravelAreExactForAllStages() {
        double[] reaches = {5, 5, 7, 3, 6}, travel = {2, 2, 4, 2, 2};
        for (int s = 1; s <= 5; s++) {
            assertEquals(reaches[s - 1], FerinBladeTrajectory.reach(s));
            assertEquals(
                    travel[s - 1],
                    FerinBladeTrajectory.travel(s, FerinBladeTrajectory.activeEnd(s)),
                    1e-9);
            for (int i = 0; i <= 100; i++) {
                var p = FerinBladeTrajectory.blade(s, i / 100.0, 0);
                double x = p.tip().right() - p.root().right(),
                        y = p.tip().up() - p.root().up(),
                        z = p.tip().forward() - p.root().forward();
                assertEquals(reaches[s - 1], Math.sqrt(x * x + y * y + z * z), 1e-8);
            }
            assertTrue(FerinBladeTrajectory.sample(s, 0).isEmpty());
            assertTrue(
                    FerinBladeTrajectory.sample(s, FerinBladeTrajectory.activeEnd(s)).isPresent());
            assertTrue(
                    FerinBladeTrajectory.sample(s, FerinBladeTrajectory.activeEnd(s) + 1)
                            .isEmpty());
        }
    }

    @Test
    void firstTwoAreOppositeDiagonalSweepsAndThirdFinishesDown() {
        var a = FerinBladeTrajectory.blade(1, 0, 0).tip();
        var b = FerinBladeTrajectory.blade(1, 1, 0).tip();
        assertTrue(a.right() < 0 && a.up() > 1.05 && b.right() > 0 && b.up() < 1.05);
        assertEquals(a, FerinBladeTrajectory.blade(2, 1, 0).tip());
        assertEquals(b, FerinBladeTrajectory.blade(2, 0, 0).tip());
        var start = FerinBladeTrajectory.blade(3, 0, 0).tip();
        var end = FerinBladeTrajectory.blade(3, 1, 0).tip();
        assertTrue(start.right() < 0 && start.up() > 1.05);
        assertEquals(0, end.right(), 1e-8);
        assertEquals(-5.95, end.up(), 1e-8);
    }

    @Test
    void ringPathsCoverRearAndReturnToRequestedFinish() {
        var four = FerinBladeTrajectory.blade(4, 1, 0).tip();
        assertEquals(0, four.right(), 1e-8);
        assertEquals(3, four.forward(), 1e-8);
        assertTrue(FerinBladeTrajectory.blade(4, 0, 0).tip().up() < 1.05);
        var five = FerinBladeTrajectory.blade(5, 1, 0).tip();
        assertEquals(-4.95, five.up(), 1e-8);
        assertEquals(0, five.forward(), 1e-8);
        assertEquals(-6, FerinBladeTrajectory.blade(5, 0.4, 0).tip().forward(), 1e-8);
    }

    @Test
    void attacksFinishBeforeDefaultComboUnlockAndLaunchKeepsWholeArc() {
        for (int s = 1; s <= 5; s++) {
            assertTrue(FerinBladeTrajectory.activeEnd(s) < 10);
            assertTrue(
                    FerinBladeTrajectory.sweeps(s, FerinBladeTrajectory.activeEnd(s)).size() > 100);
            assertEquals(
                    1, FerinBladeTrajectory.progress(s, FerinBladeTrajectory.sweepEnd(s)), 1e-9);
        }
    }
}
