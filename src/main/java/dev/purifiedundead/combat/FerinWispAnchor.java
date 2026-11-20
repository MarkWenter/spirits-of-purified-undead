package dev.purifiedundead.combat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** One continuous anchor per equipped player; no entity or chunk lookups. */
final class FerinWispAnchor {
    private Vec3 position, previousOwner, motion = Vec3.ZERO;
    private long lastTick = Long.MIN_VALUE;

    Vec3 update(Player player) {
        long tick = player.level().getGameTime();
        Vec3 owner = player.position().add(0, Math.min(1.15, player.getBbHeight() * .7), 0);
        double phase = (player.getUUID().getLeastSignificantBits() & 65535) * .001;
        return update(owner, player.getYRot(), tick, phase);
    }

    Vec3 update(Vec3 owner, float yaw, long tick, double phase) {
        boolean teleported = previousOwner != null && previousOwner.distanceToSqr(owner) > 144;
        if (lastTick == tick && !teleported) return position;
        if (teleported || position == null || tick - lastTick > 2 || tick < lastTick) {
            position = FerinWispMotion.idleAnchor(owner, yaw, tick, phase);
            motion = Vec3.ZERO;
        } else {
            Vec3 raw = owner.subtract(previousOwner);
            motion = motion.lerp(raw, .5);
            position = FerinWispMotion.step(position, owner, motion, yaw, tick, phase);
        }
        previousOwner = owner;
        lastTick = tick;
        return position;
    }
}
