package dev.purifiedundead.combat;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative swept blade geometry, independent of entity selection. */
public final class FerinSweptBlade {
    private static final int TIME_SAMPLES = 5;

    private FerinSweptBlade() { }

    public static boolean intersects(WorldPose previous, WorldPose current, AABB target) {
        double thickness = Math.max(previous.thickness(), current.thickness());
        AABB expanded = target.inflate(thickness);
        for (int step = 0; step < TIME_SAMPLES; step++) {
            double t = step / (double) (TIME_SAMPLES - 1);
            Vec3 root = previous.root().lerp(current.root(), t);
            Vec3 tip = previous.tip().lerp(current.tip(), t);
            if (segmentIntersectsBox(root, tip, expanded)) {
                return true;
            }
        }
        return false;
    }

    public static AABB bounds(WorldPose previous, WorldPose current) {
        double thickness = Math.max(previous.thickness(), current.thickness());
        double minX = Math.min(Math.min(previous.root().x, previous.tip().x),
                Math.min(current.root().x, current.tip().x));
        double minY = Math.min(Math.min(previous.root().y, previous.tip().y),
                Math.min(current.root().y, current.tip().y));
        double minZ = Math.min(Math.min(previous.root().z, previous.tip().z),
                Math.min(current.root().z, current.tip().z));
        double maxX = Math.max(Math.max(previous.root().x, previous.tip().x),
                Math.max(current.root().x, current.tip().x));
        double maxY = Math.max(Math.max(previous.root().y, previous.tip().y),
                Math.max(current.root().y, current.tip().y));
        double maxZ = Math.max(Math.max(previous.root().z, previous.tip().z),
                Math.max(current.root().z, current.tip().z));
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(thickness);
    }

    private static boolean segmentIntersectsBox(Vec3 start, Vec3 end, AABB box) {
        double[] range = {0.0, 1.0};
        return clip(start.x, end.x - start.x, box.minX, box.maxX, range)
                && clip(start.y, end.y - start.y, box.minY, box.maxY, range)
                && clip(start.z, end.z - start.z, box.minZ, box.maxZ, range);
    }

    private static boolean clip(double start, double direction, double min, double max, double[] range) {
        if (Math.abs(direction) < 1.0E-9) {
            return start >= min && start <= max;
        }
        double near = (min - start) / direction;
        double far = (max - start) / direction;
        if (near > far) {
            double swap = near;
            near = far;
            far = swap;
        }
        range[0] = Math.max(range[0], near);
        range[1] = Math.min(range[1], far);
        return range[0] <= range[1];
    }

    public record WorldPose(Vec3 root, Vec3 tip, double thickness) { }
}
