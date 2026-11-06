package dev.purifiedundead.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** One logical, quantized anchor per equipped player; no entity or chunk lookups. */
final class FerinWispAnchor {
    private Vec3 position, previousOwner, motion = Vec3.ZERO;
    private long lastTick = Long.MIN_VALUE;

    Vec3 update(Player player) {
        long tick = player.level().getGameTime();
        Vec3 owner = player.position().add(0, Math.min(1.15, player.getBbHeight() * .7), 0);
        double phase = (player.getUUID().getLeastSignificantBits() & 65535) * .001;
        if (position == null || tick - lastTick > 2 || tick < lastTick) {
            position = FerinWispMotion.idleAnchor(owner, player.getYRot(), tick, phase);
            motion = Vec3.ZERO;
        } else if (tick != lastTick) {
            Vec3 raw = owner.subtract(previousOwner);
            motion = raw.lengthSqr() > 144 ? raw : motion.lerp(raw, .5);
            position = FerinWispMotion.step(position, owner, motion, player.getYRot(), tick, phase);
        }
        previousOwner = owner;
        lastTick = tick;
        return Vec3.atCenterOf(BlockPos.containing(position));
    }
}
