package dev.purifiedundead.client;

import dev.purifiedundead.combat.FerinBladeTrajectory;
import dev.purifiedundead.entity.FerinSlashMesh;

import java.util.ArrayList;
import java.util.List;

/**
 * Small bounded trailing ribbons, sampled from the existing blade curve. Client presentation only.
 */
public final class FerinSlashEffects {
    public static final int FRAMES = 12;

    private FerinSlashEffects() {}

    public static float atlasV(float v, double age) {
        int frame = Math.floorMod((int) Math.floor(age * 1.5), FRAMES);
        return (frame + v) / FRAMES;
    }

    public static List<FerinSlashMesh.Quad> trail(int stage, double age, int lifetime) {
        if (stage < 1 || stage > 5 || !Double.isFinite(age) || age <= 0 || age >= lifetime)
            return List.of();
        double progress = Math.min(1, age / (FerinBladeTrajectory.sweepEnd(stage) - 1));
        double fade = Math.min(1, age / 2) * Math.min(1, (lifetime - age) / 3);
        int segments = 40;
        var quads = new ArrayList<FerinSlashMesh.Quad>(segments);
        for (int i = 0; i < segments; i++) {
            double a = progress * i / segments;
            double b = progress * (i + 1) / segments;
            quads.add(
                    new FerinSlashMesh.Quad(
                            point(stage, a, age, false, fade),
                            point(stage, b, age, false, fade),
                            point(stage, b, age, true, fade),
                            point(stage, a, age, true, fade)));
        }
        return quads;
    }

    private static FerinSlashMesh.Vertex point(
            int stage, double progress, double age, boolean tail, double fade) {
        var tip = FerinBladeTrajectory.blade(stage, progress, 0).tip();
        double radius = tail ? .89 + .025 * Math.sin(progress * 33 - age) : .96;
        double back = tail ? 1.15 + .3 * Math.sin(progress * 41 - age * .7) : 0;
        int alpha = tail ? 0 : (int) (80 * fade);
        return new FerinSlashMesh.Vertex(
                tip.right() * radius,
                1.05 + (tip.up() - 1.05) * radius,
                tip.forward() * radius - back,
                174,
                52,
                231,
                alpha);
    }
}
