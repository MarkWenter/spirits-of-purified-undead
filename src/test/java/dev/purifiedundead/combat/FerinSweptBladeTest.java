package dev.purifiedundead.combat;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerinSweptBladeTest {
    @Test
    void detectsABoxTouchedByTheBlade() {
        var pose = pose(new Vec3(0, 1, 0), new Vec3(2, 1, 0), 0.2);
        assertTrue(FerinSweptBlade.intersects(pose, pose, new AABB(0.9, 0.8, -0.1, 1.1, 1.2, 0.1)));
    }

    @Test
    void detectsTargetsCrossedBetweenTicks() {
        var previous = pose(new Vec3(-2, 1, 0), new Vec3(-1, 1, 0), 0.2);
        var current = pose(new Vec3(1, 1, 0), new Vec3(2, 1, 0), 0.2);
        assertTrue(
                FerinSweptBlade.intersects(
                        previous, current, new AABB(-0.2, 0.8, -0.2, 0.2, 1.2, 0.2)));
    }

    @Test
    void allowsARealMissOutsideTheSweptVolume() {
        var previous = pose(new Vec3(-2, 1, 0), new Vec3(-1, 1, 0), 0.2);
        var current = pose(new Vec3(1, 1, 0), new Vec3(2, 1, 0), 0.2);
        assertFalse(
                FerinSweptBlade.intersects(
                        previous, current, new AABB(-0.2, 3.0, -0.2, 0.2, 3.5, 0.2)));
    }

    private static FerinSweptBlade.WorldPose pose(Vec3 root, Vec3 tip, double thickness) {
        return new FerinSweptBlade.WorldPose(root, tip, thickness);
    }
}
