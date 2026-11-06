package dev.purifiedundead.client;

import net.minecraft.world.phys.Vec3;
import dev.purifiedundead.combat.FerinWispMotion;

/** Compatibility facade; motion is shared with the authoritative logical anchor. */
public final class WispMotion {
    public static final double MAX_DISTANCE = FerinWispMotion.MAX_DISTANCE;

    private WispMotion() {}

    public static int lightLevel(int warriors) {
        return FerinWispMotion.lightLevel(warriors);
    }

    public static Vec3 idleAnchor(Vec3 owner, float yaw, double time, double phase) {
        return FerinWispMotion.idleAnchor(owner, yaw, time, phase);
    }

    public static Vec3 step(Vec3 p, Vec3 owner, Vec3 motion, float yaw, double time, double phase) {
        return FerinWispMotion.step(p, owner, motion, yaw, time, phase);
    }
}
