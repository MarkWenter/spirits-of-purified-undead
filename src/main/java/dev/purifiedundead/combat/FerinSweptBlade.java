package dev.purifiedundead.combat;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative swept blade geometry, independent of entity selection. */
public final class FerinSweptBlade {
    private FerinSweptBlade() {}

    public static boolean intersects(WorldPose previous, WorldPose current, AABB target) {
        AABB expanded = target.inflate(Math.max(previous.thickness(), current.thickness()));
        // A continuous ruled blade, not five temporal samples: speed does not create holes.
        return triangle(previous.root(), previous.tip(), current.tip(), expanded)
                || triangle(previous.root(), current.tip(), current.root(), expanded);
    }

    private static boolean triangle(Vec3 a, Vec3 b, Vec3 c, AABB box) {
        if (separated(a, b, c, box, 1, 0, 0)
                || separated(a, b, c, box, 0, 1, 0)
                || separated(a, b, c, box, 0, 0, 1)) return false;
        double ux = b.x - a.x, uy = b.y - a.y, uz = b.z - a.z;
        double vx = c.x - b.x, vy = c.y - b.y, vz = c.z - b.z;
        if (separated(a, b, c, box, uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx))
            return false;
        return !edgeSeparated(a, b, c, box, ux, uy, uz)
                && !edgeSeparated(a, b, c, box, vx, vy, vz)
                && !edgeSeparated(a, b, c, box, a.x - c.x, a.y - c.y, a.z - c.z);
    }

    private static boolean edgeSeparated(
            Vec3 a, Vec3 b, Vec3 c, AABB box, double x, double y, double z) {
        return separated(a, b, c, box, 0, z, -y)
                || separated(a, b, c, box, -z, 0, x)
                || separated(a, b, c, box, y, -x, 0);
    }

    /** Allocation-free SAT axes; translated coordinates keep precision near world borders. */
    private static boolean separated(
            Vec3 a, Vec3 b, Vec3 c, AABB box, double x, double y, double z) {
        if (x * x + y * y + z * z < 1e-18) return false;
        double cx = (box.minX + box.maxX) * .5,
                cy = (box.minY + box.maxY) * .5,
                cz = (box.minZ + box.maxZ) * .5;
        double radius =
                (box.getXsize() * Math.abs(x)
                                + box.getYsize() * Math.abs(y)
                                + box.getZsize() * Math.abs(z))
                        * .5;
        double p = (a.x - cx) * x + (a.y - cy) * y + (a.z - cz) * z;
        double q = (b.x - cx) * x + (b.y - cy) * y + (b.z - cz) * z;
        double r = (c.x - cx) * x + (c.y - cy) * y + (c.z - cz) * z;
        return Math.min(p, Math.min(q, r)) > radius + 1e-9
                || Math.max(p, Math.max(q, r)) < -radius - 1e-9;
    }

    public static AABB bounds(WorldPose previous, WorldPose current) {
        double thickness = Math.max(previous.thickness(), current.thickness());
        double minX =
                Math.min(
                        Math.min(previous.root().x, previous.tip().x),
                        Math.min(current.root().x, current.tip().x));
        double minY =
                Math.min(
                        Math.min(previous.root().y, previous.tip().y),
                        Math.min(current.root().y, current.tip().y));
        double minZ =
                Math.min(
                        Math.min(previous.root().z, previous.tip().z),
                        Math.min(current.root().z, current.tip().z));
        double maxX =
                Math.max(
                        Math.max(previous.root().x, previous.tip().x),
                        Math.max(current.root().x, current.tip().x));
        double maxY =
                Math.max(
                        Math.max(previous.root().y, previous.tip().y),
                        Math.max(current.root().y, current.tip().y));
        double maxZ =
                Math.max(
                        Math.max(previous.root().z, previous.tip().z),
                        Math.max(current.root().z, current.tip().z));
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(thickness);
    }

    public record WorldPose(Vec3 root, Vec3 tip, double thickness) {}
}
