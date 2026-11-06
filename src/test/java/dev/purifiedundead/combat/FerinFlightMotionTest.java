package dev.purifiedundead.combat;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerinFlightMotionTest {
    @Test
    void reachesStationaryAimWithinDeadlineWithFourBlocksOfOverrun() {
        for (double distance : new double[] {0, .1, 4, 20, 60, 600, 4096}) {
            var f =
                    new FerinFlightMotion(
                            Vec3.ZERO, new Vec3(0, 0, distance), new Vec3(0, 0, 1), 1, 60, 6);
            assertTrue(f.speed() >= 1);
            assertTrue(distance / f.speed() <= 60 + 1e-9);
            int ticks = 0;
            while (!f.finished() && ticks++ < 100) f.step(null);
            assertTrue(f.finished());
            assertEquals(distance + 4, f.position().z, 1e-7);
        }
    }

    @Test
    void zeroTimeAndVerticalShotsAreFinite() {
        var f = new FerinFlightMotion(Vec3.ZERO, new Vec3(0, 100, 0), new Vec3(0, 1, 0), 1, 0, 6);
        f.step(null);
        assertEquals(100, f.position().y, 1e-9);
        var p = FerinFlightMotion.point(Vec3.ZERO, f.direction(), 2, 3, 4);
        assertTrue(FerinFlightMotion.finite(p));
        assertEquals(4, p.y, 1e-9);
        assertEquals(Vec3.ZERO, FerinFlightMotion.point(Vec3.ZERO, f.direction(), 0, 1.05, 0));
    }

    @Test
    void limitsTurningAndStopsChasingAtFixedBudget() {
        var f = new FerinFlightMotion(Vec3.ZERO, new Vec3(0, 0, 60), new Vec3(0, 0, 1), 1, 60, 6);
        Vec3 before = f.direction();
        f.step(new Vec3(100, 0, 0));
        assertEquals(Math.toRadians(6), Math.acos(before.dot(f.direction())), 1e-8);
        for (int i = 0; i < 100; i++) f.step(new Vec3(1000, 1000, 1000));
        assertTrue(f.finished());
        assertTrue(f.position().length() <= 64 + 1e-8);
    }

    @Test
    void deadTargetContinuesAlongLastDirection() {
        var f = new FerinFlightMotion(Vec3.ZERO, new Vec3(0, 0, 20), new Vec3(0, 0, 1), 1, 60, 6);
        f.step(new Vec3(5, 0, 20));
        Vec3 direction = f.direction(), previous = f.position();
        f.step(null);
        assertEquals(direction, f.direction());
        assertEquals(1, f.position().subtract(previous).dot(direction), 1e-8);
    }

    @Test
    void rejectsBadCoordinatesAndSanitizesInvalidTuning() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new FerinFlightMotion(
                                Vec3.ZERO, new Vec3(Double.NaN, 0, 0), Vec3.ZERO, 1, 60, 6));
        assertThrows(
                IllegalArgumentException.class,
                () -> new FerinFlightMotion(Vec3.ZERO, new Vec3(4097, 0, 0), Vec3.ZERO, 1, 60, 6));
        var f =
                new FerinFlightMotion(
                        Vec3.ZERO, new Vec3(0, 0, 2), Vec3.ZERO, Double.NaN, 60, Double.NaN);
        f.step(null);
        assertTrue(FerinFlightMotion.finite(f.position()));
    }

    @Test
    void continuousSweepHitsThinTargetsAtArbitraryHighSpeedPositions() {
        var a = new FerinSweptBlade.WorldPose(new Vec3(0, 0, 0), new Vec3(0, 5, 0), .5);
        var b = new FerinSweptBlade.WorldPose(new Vec3(1000, 0, 0), new Vec3(1000, 5, 0), .5);
        for (double x : new double[] {.01, 17.35, 91.73, 333.333, 749.9, 999.99}) {
            assertTrue(FerinSweptBlade.intersects(a, b, new AABB(x, 2, -.01, x + .01, 2.01, .01)));
            assertFalse(FerinSweptBlade.intersects(a, b, new AABB(x, 2, 1, x + .01, 2.01, 1.01)));
        }
    }

    @Test
    void hitToleranceAddsPointTwoWithoutChangingMeshOrMakingWideMissesHits() {
        var a = new FerinSweptBlade.WorldPose(Vec3.ZERO, new Vec3(0, 5, 0), .5);
        assertTrue(FerinSweptBlade.intersects(a, a, new AABB(.4, 2, 0, .41, 2.01, .01)));
        assertFalse(FerinSweptBlade.intersects(a, a, new AABB(.6, 2, 0, .61, 2.01, .01)));
    }

    @Test
    void triangleSweepDoesNotFillAnEmptyCornerOfItsBroadBox() {
        var a = new FerinSweptBlade.WorldPose(Vec3.ZERO, new Vec3(0, 10, 0), .05);
        var b = new FerinSweptBlade.WorldPose(Vec3.ZERO, new Vec3(10, 0, 0), .05);
        assertFalse(FerinSweptBlade.intersects(a, b, new AABB(8, 8, -.01, 8.1, 8.1, .01)));
        assertTrue(FerinSweptBlade.intersects(a, b, new AABB(4, 4, -.01, 4.1, 4.1, .01)));
    }
}
