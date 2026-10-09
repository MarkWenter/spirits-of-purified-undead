package dev.purifiedundead.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Shared continuous slash geometry: player-local right/up/forward, measured in blocks. */
public final class FerinBladeTrajectory {
    private FerinBladeTrajectory() {}

    public static double reach(int stage) {
        return switch (stage) {
            case 1, 2 -> 5;
            case 3 -> 7;
            case 4 -> 3;
            case 5 -> 6;
            default -> throw new IllegalArgumentException("Ferin stage must be 1..5");
        };
    }

    public static double travelDistance(int stage) {
        reach(stage);
        return stage == 3 ? 4 : 2;
    }

    public static int sweepEnd(int stage) {
        reach(stage);
        return stage <= 2 ? 3 : stage == 3 ? 4 : 5;
    }

    public static int activeEnd(int stage) {
        return sweepEnd(stage) + 3;
    }

    public static double progress(int stage, double age) {
        double t = clamp((age - 1) / (sweepEnd(stage) - 1));
        return 1 - (1 - t) * (1 - t);
    }

    public static double travel(int stage, double age) {
        return travelDistance(stage) * clamp((age - sweepEnd(stage)) / 3);
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    /** A radial blade at any point on the slash, also used by the launched wave. */
    public static BladePose blade(int stage, double progress, double travel) {
        double p = clamp(progress), x, y, z;
        if (stage == 1) {
            // Camera-left/up -> camera-right/down.
            double angle = Math.toRadians(-78 + 156 * p);
            x = Math.sin(angle) * Math.cos(Math.toRadians(27));
            y = -Math.sin(angle) * Math.sin(Math.toRadians(27));
            z = Math.cos(angle);
        } else if (stage == 2) {
            // Camera-right/down -> camera-left/up. Do not mirror this into stage 1.
            double angle = Math.toRadians(78 - 156 * p);
            x = Math.sin(angle) * Math.cos(Math.toRadians(27));
            y = -Math.sin(angle) * Math.sin(Math.toRadians(27));
            z = Math.cos(angle);
        } else if (stage == 3) {
            double angle = Math.toRadians(-82 + 172 * p);
            // Slight upper-left entry straightens into a vertical downward finish.
            double entry = Math.max(0, 1 - p / 0.45);
            double side = -0.20 * entry * entry;
            double scale = Math.sqrt(1 - side * side);
            x = side;
            y = -Math.sin(angle) * scale;
            z = Math.cos(angle) * scale;
        } else if (stage == 4) {
            if (p < 0.18) {
                double t = p / 0.18;
                // From below/front into the raised left edge of the tilted ring.
                x = -0.94 * t;
                y = -0.90 * (1 - t) + 0.342 * t;
                z = 0.436 * (1 - t);
                double length = Math.sqrt(x * x + y * y + z * z);
                x /= length;
                y /= length;
                z /= length;
            } else {
                double angle = Math.toRadians(-90 - 270 * (p - 0.18) / 0.82);
                x = Math.sin(angle) * Math.cos(Math.toRadians(20));
                y = -Math.sin(angle) * Math.sin(Math.toRadians(20));
                z = Math.cos(angle);
            }
        } else {
            reach(stage);
            // Front -> below -> behind -> above -> front -> below (450 degrees).
            double angle = Math.toRadians(450 * p);
            x = 0;
            y = -Math.sin(angle);
            z = Math.cos(angle);
        }
        double length = reach(stage);
        Point root = new Point(0, 1.05, travel);
        return new BladePose(
                root, new Point(x * length, 1.05 + y * length, travel + z * length), 0.30);
    }

    public static Optional<BladePose> sample(int stage, int ageTicks) {
        if (ageTicks < 1 || ageTicks > activeEnd(stage)) return Optional.empty();
        return Optional.of(blade(stage, progress(stage, ageTicks), travel(stage, ageTicks)));
    }

    /** Fine curved substeps avoid replacing a full ring with a chord between two tick poses. */
    public static List<Sweep> sweeps(int stage, int age) {
        if (age < 1 || age > activeEnd(stage)) return List.of();
        double from = Math.max(1, age - 1), to = age;
        List<Sweep> result = new ArrayList<>();
        if (from < sweepEnd(stage) || age == 1) {
            double a = progress(stage, from), b = progress(stage, Math.min(to, sweepEnd(stage)));
            int steps = Math.max(1, (int) Math.ceil((b - a) * 180));
            for (int i = 0; i < steps; i++)
                result.add(
                        new Sweep(
                                blade(stage, a + (b - a) * i / steps, 0),
                                blade(stage, a + (b - a) * (i + 1) / steps, 0)));
        }
        if (to > sweepEnd(stage)) {
            double start = travel(stage, from), end = travel(stage, to);
            // Whole fan/ring travels, not just its final endpoint; interiors remain hittable.
            for (int i = 0; i <= 180; i++) {
                var a = blade(stage, i / 180.0, start);
                var b = blade(stage, i / 180.0, end);
                result.add(new Sweep(a, b));
                // Keep the corridor from the player to the travelling blade damaging too.
                Point origin = new Point(0, 1.05, 0);
                result.add(
                        new Sweep(
                                new BladePose(origin, a.tip(), a.thickness()),
                                new BladePose(origin, b.tip(), b.thickness())));
            }
        }
        return result;
    }

    public record Point(double right, double up, double forward) {}

    public record BladePose(Point root, Point tip, double thickness) {}

    public record Sweep(BladePose from, BladePose to) {}
}
