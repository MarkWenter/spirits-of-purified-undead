package dev.purifiedundead.client;

import net.minecraft.world.phys.Vec3;

/** Bounded, collision-free cosmetic motion. No world or lighting queries. */
public final class WispMotion {
    public static final double MAX_DISTANCE = 4.0;

    private WispMotion() {}

    public static int lightLevel(int warriors) {
        return 3 + (int) Math.round(12.0 * Math.max(0, Math.min(8, warriors)) / 8.0);
    }

    public static Vec3 idleAnchor(Vec3 owner, float yaw, double time, double phase) {
        double angle = Math.toRadians(yaw);
        Vec3 front = new Vec3(-Math.sin(angle), 0, Math.cos(angle));
        Vec3 left = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        // The smooth pseudo-random drift stays within 0.5 blocks of the left-front anchor.
        return owner.add(front.scale(.65))
                .add(left.scale(.75))
                .add(
                        .28 * Math.sin(time * .027 + phase),
                        .18 * Math.sin(time * .041 + phase * 2),
                        .28 * Math.sin(time * .019 + phase * 3));
    }

    public static Vec3 step(
            Vec3 position, Vec3 owner, Vec3 ownerMotion, float yaw, double time, double phase) {
        double speed = ownerMotion.length();
        Vec3 idle = idleAnchor(owner, yaw, time, phase);
        if (speed > 12 || position.distanceToSqr(owner) > 256) return idle;
        boolean moving = speed > .025;
        Vec3 target = idle;
        if (moving) {
            Vec3 direction = new Vec3(ownerMotion.x, 0, ownerMotion.z);
            if (direction.lengthSqr() < .0001)
                direction =
                        new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
            target = owner.subtract(direction.normalize().scale(1.4));
        }
        Vec3 delta = target.subtract(position);
        double gap = delta.length();
        double previousGap = Math.max(0, position.distanceTo(owner) - speed);
        double ratio = Math.max(0, Math.min(1, (previousGap - 1) / (MAX_DISTANCE - 1)));
        double pace =
                moving
                        ? Math.max(.025, speed * (.72 + .28 * ratio * ratio * (3 - 2 * ratio)))
                        : .025 + gap * .08;
        Vec3 next = gap < .00001 ? target : position.add(delta.scale(Math.min(gap, pace) / gap));
        Vec3 offset = next.subtract(owner);
        // Turns, knockback and large movement packets cannot leave an unbounded trailing light.
        if (offset.lengthSqr() > MAX_DISTANCE * MAX_DISTANCE)
            next = owner.add(offset.normalize().scale(MAX_DISTANCE));
        return next;
    }
}
