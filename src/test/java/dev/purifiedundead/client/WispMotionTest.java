package dev.purifiedundead.client;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WispMotionTest {
    @Test
    void lightIncreasesAndClampsToGlowstone() {
        assertEquals(3, WispMotion.lightLevel(-1));
        int last = 0;
        for (int i = 0; i <= 8; i++) {
            int n = WispMotion.lightLevel(i);
            assertTrue(n > last);
            last = n;
        }
        assertEquals(15, WispMotion.lightLevel(8));
        assertEquals(15, WispMotion.lightLevel(64));
    }

    @Test
    void idleStaysWithinOneBlockOfLeftFront() {
        Vec3 center = new Vec3(.75, 0, .65);
        for (int tick = 0; tick < 10000; tick += 7)
            assertTrue(WispMotion.idleAnchor(Vec3.ZERO, 0, tick, 2.3).distanceTo(center) < 1);
    }

    @Test
    void catchesMoreQuicklyWhenFurtherAwayAndMatchesSpeedAtLimit() {
        Vec3 owner = new Vec3(0, 0, .2), velocity = new Vec3(0, 0, .2);
        Vec3 near = new Vec3(0, 0, -2), far = new Vec3(0, 0, -3.5), limit = new Vec3(0, 0, -4);
        double a = WispMotion.step(near, owner, velocity, 0, 0, 0).distanceTo(near);
        double b = WispMotion.step(far, owner, velocity, 0, 0, 0).distanceTo(far);
        assertTrue(a < b && b < .2);
        assertEquals(.2, WispMotion.step(limit, owner, velocity, 0, 0, 0).distanceTo(limit), 1e-9);
    }

    @Test
    void turnsVerticalMotionAndFastFlightStayBounded() {
        Vec3 position = Vec3.ZERO, owner = Vec3.ZERO;
        for (int i = 0; i < 50000; i++) {
            Vec3 velocity =
                    new Vec3(
                            Math.sin(i * .07) * 1.8,
                            Math.sin(i * .03) * .8,
                            Math.cos(i * .05) * 1.8);
            owner = owner.add(velocity);
            position = WispMotion.step(position, owner, velocity, 90, i, 2);
            assertTrue(position.distanceTo(owner) <= 4.000001);
        }
    }

    @Test
    void teleportResetsInsteadOfDraggingAcrossChunks() {
        Vec3 owner = new Vec3(1000, 50, -1000);
        assertTrue(WispMotion.step(Vec3.ZERO, owner, owner, 0, 1, 2).distanceTo(owner) < 2);
    }

    @Test
    void stoppingReturnsToTheLeftFront() {
        Vec3 position = new Vec3(0, 0, -4);
        for (int i = 0; i < 500; i++)
            position = WispMotion.step(position, Vec3.ZERO, Vec3.ZERO, 0, i, 0);
        assertTrue(position.x > 0 && position.z > 0);
        assertTrue(position.distanceTo(new Vec3(.75, 0, .65)) < 1);
    }
}
