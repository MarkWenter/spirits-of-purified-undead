package dev.purifiedundead.combat;

import net.minecraft.world.phys.Vec3;

/** Identical prediction/server arithmetic. Looking vertically still dashes along the facing yaw. */
public final class GuardianMotion {
    private GuardianMotion() {}

    public static boolean validSettings(double jump, double dash) {
        return Double.isFinite(jump)
                && Double.isFinite(dash)
                && jump >= 0
                && jump <= 10
                && dash >= 0
                && dash <= 10;
    }

    public static Vec3 jump(Vec3 current, double height) {
        return new Vec3(current.x, height, current.z);
    }

    public static Vec3 dash(Vec3 current, float yaw, double speed) {
        double angle = Math.toRadians(yaw);
        return new Vec3(
                -Math.sin(angle) * speed, Math.max(0.08D, current.y), Math.cos(angle) * speed);
    }
}
