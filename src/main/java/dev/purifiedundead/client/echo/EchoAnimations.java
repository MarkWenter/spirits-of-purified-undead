package dev.purifiedundead.client.echo;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Client extension point. Bone names are case sensitive. Time is local to this release, in ticks. */
public final class EchoAnimations {
    public interface Skeleton {
        boolean root(String name);
        boolean child(String name, String parent);
    }

    public interface Rotations {
        /** Add local XYZ rotation in radians to the native pose for this draw. */
        void rotate(String bone, float x, float y, float z);
    }

    public interface Motion {
        boolean matches(Skeleton skeleton);
        /** Return false without writing any channels when this stage has no clip. */
        boolean sample(int stage, float age, Rotations pose);
    }

    private static final CopyOnWriteArrayList<Motion> MOTIONS = new CopyOnWriteArrayList<>();
    private EchoAnimations() {}

    public static void register(Motion motion) { MOTIONS.addIfAbsent(Objects.requireNonNull(motion)); }
    public static void unregister(Motion motion) { MOTIONS.remove(motion); }

    public static boolean wineFoxGecko(Skeleton s) {
        return (s.root("MRoot") || s.child("MRoot", "GuiRoot")) && s.child("Root", "MRoot") && s.child("UpperBody", "MUpperBody")
                && s.child("Arm", "UpperBody") && s.child("RightArm", "Arm") && s.child("LeftArm", "Arm")
                && s.child("RightForeArm", "RightArm") && s.child("RightHand", "RightForeArm")
                && s.child("LeftForeArm", "LeftArm") && s.child("LeftHand", "LeftForeArm")
                && s.child("Head", "MHead") && s.child("RightLeg", "DownBody") && s.child("LeftLeg", "DownBody");
    }

    public static boolean wineFoxClassic(Skeleton s) {
        return s.root("head") && s.root("body") && s.root("armLeft") && s.root("armRight")
                && s.root("legLeft") && s.root("legRight") && s.child("ear", "head") && s.child("ear2", "head")
                && s.child("sittingRotationSkirt", "body") && s.child("skirt", "sittingRotationSkirt");
    }

    static boolean enabled() {
        return !MOTIONS.isEmpty() && dev.purifiedundead.config.PurifiedUndeadConfig.ECHO_SPECIAL_ANIMATIONS.get();
    }

    static void sample(Skeleton rig, int stage, float age, Rotations out) {
        for (var motion : MOTIONS) {
            if (motion.matches(rig) && motion.sample(stage, age, out)) return;
        }
    }
}
