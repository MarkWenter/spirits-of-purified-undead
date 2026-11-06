package dev.purifiedundead.combat;

import dev.purifiedundead.entity.FerinEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import java.util.UUID;

/** Per-release context. Later combo stages cannot overwrite this flight's damage or hit set. */
final class FerinSlashFlight {
    final FerinPlayerState damage;
    final FerinFlightMotion motion;
    final long startedAt;
    final int stage;
    UUID target;
    final Vec3 targetOffset;
    FerinEntity visual;

    FerinSlashFlight(
            FerinPlayerState source,
            Vec3 anchor,
            Vec3 aim,
            Vec3 fallback,
            LivingEntity victim,
            double speed,
            int ticks,
            double turn) {
        damage = new FerinPlayerState();
        damage.combo = FerinComboState.restore(source.combo.snapshot());
        damage.triggerPreDefenseDamage = source.triggerPreDefenseDamage;
        damage.stageCriticalMultiplier = source.stageCriticalMultiplier;
        stage = source.combo.stage();
        startedAt = source.combo.stageStartedAt();
        motion = new FerinFlightMotion(anchor, aim, fallback, speed, ticks, turn);
        target = victim != null && victim.isAlive() ? victim.getUUID() : null;
        targetOffset = victim == null ? Vec3.ZERO : aim.subtract(victim.position());
    }

    Vec3 targetPosition(ServerLevel level) {
        if (target == null) return null;
        if (!(level.getEntity(target) instanceof LivingEntity living) || !living.isAlive()) {
            target = null; // Loss is terminal for this flight. Never acquire a different unit.
            return null;
        }
        var b = living.getBoundingBox();
        Vec3 point = living.position().add(targetOffset);
        return new Vec3(
                Mth.clamp(point.x, b.minX, b.maxX),
                Mth.clamp(point.y, b.minY, b.maxY),
                Mth.clamp(point.z, b.minZ, b.maxZ));
    }

    void discard() {
        if (visual != null) visual.discard();
    }
}
