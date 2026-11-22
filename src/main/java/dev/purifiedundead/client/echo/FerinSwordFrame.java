package dev.purifiedundead.client.echo;

import dev.purifiedundead.combat.FerinBladeTrajectory;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Wine Fox model space (+Y up, -Z forward). The blade face spans the ray and sweep tangent. */
final class FerinSwordFrame {
    private FerinSwordFrame() {}

    static Vector3f ray(int stage, double progress) {
        var blade = FerinBladeTrajectory.blade(stage, progress, 0);
        return new Vector3f((float) blade.tip().right(), (float) (blade.tip().up() - blade.root().up()),
                (float) -blade.tip().forward()).normalize();
    }

    static Vector3f normal(int stage, double progress) {
        // Nonsingular even during the hold: differentiate the trajectory by progress, not by clock time.
        var before = ray(stage, Math.max(0, progress - .001));
        var after = ray(stage, Math.min(1, progress + .001));
        var normal = before.cross(after).normalize();
        // Choose the same face for both directions of a double-edged cut and across grip changes.
        if ((stage == 3 || stage == 5 ? normal.x : normal.y) < 0) normal.negate();
        return normal;
    }

    static void orient(Vector3f ray, Vector3f normal, Quaternionf result) {
        var edge = ray.cross(normal, new Vector3f()).normalize();
        var face = edge.cross(ray, new Vector3f());
        new Matrix3f().setColumn(0, edge).setColumn(1, ray).setColumn(2, face).getNormalizedRotation(result);
    }
}
