package dev.purifiedundead.entity;

import dev.purifiedundead.combat.FerinBladeTrajectory;

import java.util.ArrayList;
import java.util.List;

/** Continuous fan/ring: shared collision curve, emissive blade, colored wake and fine filaments. */
public final class FerinSlashMesh {
    private static final double[] RADII = {
        0.08, 0.22, 0.35, 0.44, 0.54, 0.65, 0.76, 0.87, 0.935, 0.965, 0.98, 1.0, 1.025
    };
    private static final int[][] COLORS = {
        {65, 12, 105, 0},
        {83, 18, 145, 28},
        {105, 22, 185, 65},
        {125, 25, 212, 105},
        {114, 35, 224, 145},
        {119, 48, 231, 170},
        {158, 57, 230, 190},
        {198, 77, 225, 200},
        {232, 120, 212, 205},
        {243, 177, 201, 215},
        {239, 109, 143, 180},
        {218, 47, 78, 130},
        {180, 24, 72, 0}
    };
    private static final double[] EDGE = {-1, -0.7, -0.25, -0.07, 0.07, 0.25, 0.7, 1};
    private static final int[][] EDGE_COLORS = {
        {70, 20, 170, 0},
        {88, 35, 192, 45},
        {142, 57, 210, 100},
        {200, 99, 220, 130},
        {225, 144, 209, 140},
        {216, 96, 179, 120},
        {210, 47, 89, 65},
        {180, 25, 65, 0}
    };

    public static double faceTiltDegrees(int stage) {
        return stage == 2 ? -8 : 8;
    }

    /** Tail remains faint after release, so opposite sweeps cannot become the same solid arc. */
    public static double directionalWeight(double u) {
        return 0.12 + 0.88 * u * u;
    }

    private FerinSlashMesh() {}

    public static List<Quad> sample(int stage, double age) {
        return sample(stage, age, false, 0);
    }

    public static List<Quad> flight(int stage, double age, int lifetime) {
        if (stage < 1
                || stage > 5
                || lifetime <= 0
                || !Double.isFinite(age)
                || age < 0
                || age >= lifetime + 1) return List.of();
        double forming = Math.min(lifetime, FerinBladeTrajectory.sweepEnd(stage) - 1);
        double visualAge =
                1 + Math.min(age, forming) * (FerinBladeTrajectory.sweepEnd(stage) - 1) / forming;
        return sample(
                stage,
                visualAge,
                true,
                Math.min(1, (lifetime + 1 - age) / Math.min(3.0, lifetime)),
                age);
    }

    /** Short stationary sweep at the server-confirmed release pose. */
    public static List<Quad> launch(int stage, double age) {
        if (stage < 1 || stage > 5 || !Double.isFinite(age) || age < 0) return List.of();
        double end = FerinBladeTrajectory.sweepEnd(stage);
        if (age >= end + 2) return List.of();
        return sample(stage, Math.min(1 + age, end), true, Math.min(1, (end + 2 - age) / 2), age);
    }

    private static List<Quad> sample(int stage, double age, boolean flying, double flightFade) {
        return sample(stage, age, flying, flightFade, 0);
    }

    private static List<Quad> sample(
            int stage, double age, boolean flying, double flightFade, double effectAge) {
        if (stage < 1
                || stage > 5
                || !Double.isFinite(age)
                || age <= 1
                || age >= FerinBladeTrajectory.activeEnd(stage) + 1) return List.of();
        double progress = FerinBladeTrajectory.progress(stage, age);
        double travel = flying ? 0 : FerinBladeTrajectory.travel(stage, age);
        double fade =
                Math.min(1, (age - 1) * 3)
                        * Math.min(1, FerinBladeTrajectory.activeEnd(stage) + 1 - age);
        if (flying) fade *= flightFade;
        int segments = Math.max(4, (int) Math.ceil(progress * (stage >= 4 ? 144 : 80)));
        List<Quad> result = new ArrayList<>(segments * 23 + 18);
        Vertex[] leftFace = new Vertex[RADII.length], rightFace = new Vertex[RADII.length];
        Vertex[] leftEdge = new Vertex[EDGE.length], rightEdge = new Vertex[EDGE.length];
        for (int band = 0; band < RADII.length; band++)
            leftFace[band] =
                    vertex(
                            stage,
                            progress,
                            travel,
                            0,
                            band,
                            fade,
                            flying ? effectAge : age,
                            flying);
        for (int band = 0; band < EDGE.length; band++)
            leftEdge[band] = edge(stage, progress, travel, 0, band, fade);
        for (int i = 0; i < segments; i++) {
            double a = i / (double) segments, b = (i + 1.0) / segments;
            for (int band = 0; band < RADII.length; band++)
                rightFace[band] =
                        vertex(
                                stage,
                                progress,
                                travel,
                                b,
                                band,
                                fade,
                                flying ? effectAge : age,
                                flying);
            for (int band = 0; band < EDGE.length; band++)
                rightEdge[band] = edge(stage, progress, travel, b, band, fade);
            for (int band = 0; band < RADII.length - 1; band++)
                result.add(
                        new Quad(
                                leftFace[band],
                                rightFace[band],
                                rightFace[band + 1],
                                leftFace[band + 1]));
            // A second cross-section gives the blade real thickness when seen edge-on.
            for (int band = 0; band < EDGE.length - 1; band++)
                result.add(
                        new Quad(
                                leftEdge[band],
                                rightEdge[band],
                                rightEdge[band + 1],
                                leftEdge[band + 1]));
            // Two narrow, interrupted pink/violet filaments inside the broad wake.
            for (int streak = 0; streak < 4; streak++) {
                if ((i + streak * 3) % 17 > 11) continue;
                double r =
                        0.43
                                + streak * 0.13
                                + 0.035 * Math.sin(a * 27 + streak * 4 - effectAge * 0.8);
                int alpha =
                        (int)
                                (95
                                        * fade
                                        * directionalWeight((a + b) / 2)
                                        * Math.pow(Math.sin(Math.PI * (a + b) / 2), 0.4));
                result.add(
                        new Quad(
                                at(stage, progress * a, travel, r, 166, 60, 219, alpha),
                                at(stage, progress * b, travel, r, 166, 60, 219, alpha),
                                at(stage, progress * b, travel, r + 0.015, 212, 119, 228, alpha),
                                at(stage, progress * a, travel, r + 0.015, 212, 119, 228, alpha)));
            }
            Vertex[] swap = leftFace;
            leftFace = rightFace;
            rightFace = swap;
            swap = leftEdge;
            leftEdge = rightEdge;
            rightEdge = swap;
        }
        // Small outward sparks follow the arc; no camera-facing oversized particles.
        for (int i = 1; i < 18; i++) {
            double p = i / 18.0;
            if (p > progress) break;
            double burst = Math.max(0, (age - 1) - p * (FerinBladeTrajectory.sweepEnd(stage) - 1));
            if (burst > 3) continue;
            double r = 1 + burst * 0.025;
            int alpha = (int) (140 * fade * (1 - burst / 3));
            double size = 0.007;
            result.add(
                    new Quad(
                            at(stage, p - size, travel, r, 170, 100, 255, 0),
                            at(stage, p, travel, r + 0.026, 229, 148, 198, alpha),
                            at(stage, p + size, travel, r, 255, 94, 160, 0),
                            at(stage, p, travel, r - 0.026, 230, 121, 255, alpha)));
        }
        return result;
    }

    private static Vertex vertex(
            int stage,
            double progress,
            double travel,
            double u,
            int band,
            double fade,
            double age,
            boolean dynamic) {
        int[] c = COLORS[band];
        double taper = Math.pow(Math.max(0, Math.sin(Math.PI * u)), 0.28);
        // Preserve the full ring near closure; otherwise taper the two ends sharply.
        if (stage >= 4 && progress > 0.99) taper = 0.65 + 0.35 * taper;
        double waveTime = dynamic ? age : 0;
        double tooth =
                band < 7
                        ? 0.05
                                * Math.sin(u * 73 + band * 0.8 - waveTime * 0.9)
                                * Math.sin(u * 31 + waveTime * 0.55)
                        : 0;
        double radius = 1 - (1 - RADII[band]) * taper + tooth * taper;
        double wake = stage <= 2 ? directionalWeight(u) : 0.70 + 0.30 * u;
        double ripple = band < 6 ? 0.70 + 0.30 * Math.sin(u * 47 - age * 3 + band) : 1;
        int alpha = (int) Math.round(c[3] * fade * taper * wake * ripple);
        // Anchor the tiled grain to the swept angle so it uncovers as the blade forms.
        float texU = (float) (progress * u * (stage >= 4 ? 2 : 1));
        float texV = (float) ((0.5 + 126 * RADII[band] / 1.025) / 128);
        return at(stage, progress * u, travel, radius, c[0], c[1], c[2], alpha, texU, texV);
    }

    private static Vertex edge(
            int stage, double progress, double travel, double u, int band, double fade) {
        double p = progress * u;
        var pose = FerinBladeTrajectory.blade(stage, p, travel);
        var before = FerinBladeTrajectory.blade(stage, Math.max(0, p - 0.001), travel).tip();
        var after = FerinBladeTrajectory.blade(stage, Math.min(1, p + 0.001), travel).tip();
        var root = pose.root();
        var tip = pose.tip();
        double dx = tip.right() - root.right(),
                dy = tip.up() - root.up(),
                dz = tip.forward() - root.forward();
        double tx = after.right() - before.right(),
                ty = after.up() - before.up(),
                tz = after.forward() - before.forward();
        double nx = dy * tz - dz * ty, ny = dz * tx - dx * tz, nz = dx * ty - dy * tx;
        double length = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1e-9) length = 1;
        double taper = Math.pow(Math.max(0, Math.sin(Math.PI * u)), 0.3);
        if (stage >= 4 && progress > 0.99) taper = 0.65 + 0.35 * taper;
        double offset = EDGE[band] * 0.22 * taper / length;
        int[] c = EDGE_COLORS[band];
        return new Vertex(
                root.right() + dx * .975 + nx * offset,
                root.up() + dy * .975 + ny * offset,
                root.forward() + dz * .975 + nz * offset,
                c[0],
                c[1],
                c[2],
                (int) (c[3] * fade * taper * (stage <= 2 ? directionalWeight(u) : 1)));
    }

    private static Vertex at(
            int stage, double p, double travel, double radius, int r, int g, int b, int alpha) {
        return at(stage, p, travel, radius, r, g, b, alpha, 0.5F, SOLID_V);
    }

    private static Vertex at(
            int stage,
            double p,
            double travel,
            double radius,
            int r,
            int g,
            int b,
            int alpha,
            float texU,
            float texV) {
        var pose = FerinBladeTrajectory.blade(stage, p, travel);
        var root = pose.root();
        var tip = pose.tip();
        // Bevel the ribbon about its outer cutting edge. Reach/outer path stay exact;
        // the broad inner face turns toward the viewer instead of lying edge-on.
        var before = FerinBladeTrajectory.blade(stage, Math.max(0, p - 0.001), travel).tip();
        var after = FerinBladeTrajectory.blade(stage, Math.min(1, p + 0.001), travel).tip();
        double dx = tip.right() - root.right(),
                dy = tip.up() - root.up(),
                dz = tip.forward() - root.forward();
        double tx = after.right() - before.right(),
                ty = after.up() - before.up(),
                tz = after.forward() - before.forward();
        double nx = dy * tz - dz * ty, ny = dz * tx - dx * tz, nz = dx * ty - dy * tx;
        // Reverse traversal also reverses its cross product: canonicalize stage 2 first.
        if (stage == 2) {
            nx = -nx;
            ny = -ny;
            nz = -nz;
        }
        double norm = Math.sqrt(nx * nx + ny * ny + nz * nz);
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double angle = Math.toRadians(faceTiltDegrees(stage));
        if (stage >= 4) radius = 1 - (1 - radius) * 0.42;
        double inward = (1 - radius) * Math.cos(angle);
        double bevel = norm < 1e-9 ? 0 : (1 - radius) * length * Math.sin(angle) / norm;
        return new Vertex(
                tip.right() - dx * inward + nx * bevel,
                tip.up() - dy * inward + ny * bevel,
                tip.forward() - dz * inward + nz * bevel,
                r,
                g,
                b,
                alpha,
                texU,
                texV);
    }

    private static final float SOLID_V = 127.5F / 128;

    public record Vertex(
            double right,
            double up,
            double forward,
            int red,
            int green,
            int blue,
            int alpha,
            float texU,
            float texV) {
        public Vertex(
                double right, double up, double forward, int red, int green, int blue, int alpha) {
            this(right, up, forward, red, green, blue, alpha, 0.5F, SOLID_V);
        }

        public boolean texturedFace() {
            return texV < SOLID_V;
        }
    }

    public record Quad(Vertex a, Vertex b, Vertex c, Vertex d) {}
}
