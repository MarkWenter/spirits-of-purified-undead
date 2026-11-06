package dev.purifiedundead.combat;

import net.minecraft.world.phys.Vec3;

/** Bounded, world-independent flight. A fixed deadline never becomes an endless chase. */
public final class FerinFlightMotion {
    public static final double EXTRA_RANGE = 4;
    public static final double MAX_DISTANCE = 4096;
    private Vec3 position, direction;
    private final double speed, turnRadians;
    private double remaining;
    private final int lifetime;
    private int age;

    public FerinFlightMotion(
            Vec3 origin,
            Vec3 aim,
            Vec3 fallback,
            double minimumSpeed,
            int arrivalTicks,
            double turnDegrees) {
        if (!finite(origin) || !finite(aim) || !finite(fallback))
            throw new IllegalArgumentException("Non-finite Ferin flight");
        if (!Double.isFinite(minimumSpeed)) minimumSpeed = 1;
        if (!Double.isFinite(turnDegrees)) turnDegrees = 6;
        position = origin;
        Vec3 offset = aim.subtract(origin);
        double distance = offset.length();
        if (distance > MAX_DISTANCE)
            throw new IllegalArgumentException("Ferin flight exceeds safety range");
        direction = distance < 1e-6 ? fallback.normalize() : offset.scale(1 / distance);
        if (direction.lengthSqr() < .5) direction = new Vec3(0, 0, 1);
        // Zero seconds means the next authoritative tick, never division by zero.
        speed =
                Math.max(
                        Math.max(.25, Math.min(64, minimumSpeed)),
                        distance / Math.max(1, Math.min(60, arrivalTicks)));
        remaining = distance + EXTRA_RANGE;
        lifetime = Math.max(1, (int) Math.ceil(remaining / speed));
        turnRadians = Math.toRadians(Math.max(0, Math.min(30, turnDegrees)));
    }

    public void step(Vec3 target) {
        if (finished()) return;
        if (target != null && finite(target)) {
            Vec3 delta = target.subtract(position);
            if (delta.lengthSqr() > 1e-8)
                direction = turn(direction, delta.normalize(), turnRadians);
        }
        double travel = Math.min(speed, remaining);
        position = position.add(direction.scale(travel));
        remaining = Math.max(0, remaining - travel);
        age++;
    }

    public static Vec3 turn(Vec3 from, Vec3 to, double limit) {
        double dot = Math.max(-1, Math.min(1, from.dot(to)));
        double angle = Math.acos(dot);
        if (angle <= limit || angle < 1e-8) return to;
        Vec3 tangent = to.subtract(from.scale(dot));
        if (tangent.lengthSqr() < 1e-12) return from; // No arbitrary U-turn at an antipode.
        return from.scale(Math.cos(limit))
                .add(tangent.normalize().scale(Math.sin(limit)))
                .normalize();
    }

    public static boolean finite(Vec3 v) {
        return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }

    public Vec3 position() {
        return position;
    }

    public Vec3 direction() {
        return direction;
    }

    public double speed() {
        return speed;
    }

    public int lifetime() {
        return lifetime;
    }

    public int age() {
        return age;
    }

    public boolean finished() {
        return age >= lifetime || remaining < 1e-7;
    }

    /** Shared full 3-D basis; local blade's historical 1.05 pivot is moved to the wisp. */
    public static Vec3 point(Vec3 origin, Vec3 forward, double right, double up, double ahead) {
        return basis(forward).point(origin, right, up, ahead);
    }

    public static Basis basis(Vec3 forward) {
        Vec3 side = new Vec3(-forward.z, 0, forward.x);
        if (side.lengthSqr() < 1e-8) side = new Vec3(-1, 0, 0);
        else side = side.normalize();
        return new Basis(side, forward.cross(side).scale(-1).normalize(), forward);
    }

    public record Basis(Vec3 side, Vec3 vertical, Vec3 forward) {
        public Vec3 point(Vec3 origin, double right, double up, double ahead) {
            return new Vec3(
                    origin.x + side.x * right + vertical.x * (up - 1.05) + forward.x * ahead,
                    origin.y + side.y * right + vertical.y * (up - 1.05) + forward.y * ahead,
                    origin.z + side.z * right + vertical.z * (up - 1.05) + forward.z * ahead);
        }
    }
}
