package dev.purifiedundead.combat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.UUID;

/** Complete server-owned Ferin runtime state that is safe to serialize with a player. */
public final class FerinPlayerState {
    private static final int VERSION = 1;
    private static final int MAX_SAVED_TARGETS = 256;

    /** Player-centered slash origin shared by rendering and authoritative collision. */
    static final double COMPANION_LEFT_OFFSET = 0.0D;

    static final double COMPANION_FORWARD_OFFSET = 0.0D;

    final FerinContinuationBuffer continuation = new FerinContinuationBuffer();
    float continuationDamage;
    Vec3 continuationTarget;
    long lastContinuationTick = Long.MIN_VALUE;
    FerinComboState combo = new FerinComboState();
    FerinMeleeTrigger.AttackTickGate attackTickGate = new FerinMeleeTrigger.AttackTickGate();
    Vec3 origin = Vec3.ZERO;
    Vec3 forward = new Vec3(0.0, 0.0, 1.0);
    Vec3 right = new Vec3(-1.0, 0.0, 0.0);
    float triggerPreDefenseDamage;
    float stageCriticalMultiplier = 1.0F;
    FerinSweptBlade.WorldPose previousBlade;
    final HashSet<UUID> hitTargets = new HashSet<>();

    void captureStage(
            Vec3 playerPosition,
            float yawDegrees,
            float preDefenseDamage,
            float criticalMultiplier) {
        float radians = yawDegrees * ((float) Math.PI / 180.0F);
        captureStageWithForward(
                playerPosition,
                new Vec3(-Math.sin(radians), 0.0, Math.cos(radians)),
                preDefenseDamage,
                criticalMultiplier);
    }

    void captureStageTowards(
            Vec3 playerPosition,
            Vec3 targetPosition,
            float fallbackYawDegrees,
            float preDefenseDamage,
            float criticalMultiplier) {
        Vec3 towardTarget = targetPosition.subtract(playerPosition).multiply(1.0D, 0.0D, 1.0D);
        if (towardTarget.lengthSqr() < 1.0E-4D) {
            captureStage(playerPosition, fallbackYawDegrees, preDefenseDamage, criticalMultiplier);
            return;
        }
        captureStageWithForward(
                playerPosition, towardTarget.normalize(), preDefenseDamage, criticalMultiplier);
    }

    private void captureStageWithForward(
            Vec3 playerPosition,
            Vec3 stageForward,
            float preDefenseDamage,
            float criticalMultiplier) {
        forward = stageForward;
        right = new Vec3(-forward.z, 0.0, forward.x);
        origin = playerPosition;
        triggerPreDefenseDamage = preDefenseDamage;
        stageCriticalMultiplier = criticalMultiplier;
        previousBlade = null;
        hitTargets.clear();
        continuation.clear();
    }

    FerinSweptBlade.WorldPose toWorld(FerinBladeTrajectory.BladePose pose) {
        return new FerinSweptBlade.WorldPose(
                pointToWorld(pose.root()), pointToWorld(pose.tip()), pose.thickness());
    }

    Vec3 companionAnchor() {
        return origin.add(right.scale(COMPANION_LEFT_OFFSET))
                .add(forward.scale(COMPANION_FORWARD_OFFSET));
    }

    float capturedYawDegrees() {
        return (float) Math.toDegrees(Math.atan2(-forward.x, forward.z));
    }

    /**
     * GeckoLib's model front is the opposite of the captured combat-forward vector.
     * Keep this visual correction separate so the server blade path remains unchanged.
     */
    float ferinVisualYawDegrees() {
        return capturedYawDegrees() + 180.0F;
    }

    void cancelActive() {
        combo.cancelActive();
        continuation.clear();
        continuationTarget = null;
        continuationDamage = 0;
        hitTargets.clear();
        previousBlade = null;
    }

    void clearPreviousBlade() {
        previousBlade = null;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        FerinComboState.Snapshot snapshot = combo.snapshot();
        tag.putInt("version", VERSION);
        tag.putInt("stage", snapshot.stage());
        tag.putLong("stage_started_at", snapshot.stageStartedAt());
        tag.putLong("cooldown_until", snapshot.cooldownUntil());
        tag.putLong("exit_until", snapshot.exitUntil());
        tag.putBoolean("exiting", snapshot.exiting());
        tag.putLong("last_attack_tick", attackTickGate.lastAcceptedTick());
        putVec(tag, "origin", origin);
        putVec(tag, "forward", forward);
        putVec(tag, "right", right);
        tag.putFloat("damage_snapshot", triggerPreDefenseDamage);
        tag.putFloat("critical_multiplier", stageCriticalMultiplier);
        long[] targets = new long[Math.min(hitTargets.size(), MAX_SAVED_TARGETS) * 2];
        int index = 0;
        for (UUID target : hitTargets) {
            if (index >= targets.length) break;
            targets[index++] = target.getMostSignificantBits();
            targets[index++] = target.getLeastSignificantBits();
        }
        tag.putLongArray("hit_targets", targets);
        return tag;
    }

    public static FerinPlayerState load(CompoundTag tag) {
        if (tag.isEmpty() || tag.getInt("version") != VERSION) {
            return new FerinPlayerState();
        }
        try {
            var state = new FerinPlayerState();
            state.combo =
                    FerinComboState.restore(
                            new FerinComboState.Snapshot(
                                    tag.getInt("stage"),
                                    tag.getLong("stage_started_at"),
                                    tag.getLong("cooldown_until"),
                                    tag.getLong("exit_until"),
                                    tag.getBoolean("exiting")));
            state.attackTickGate =
                    new FerinMeleeTrigger.AttackTickGate(tag.getLong("last_attack_tick"));
            state.origin = getFiniteVec(tag, "origin", Vec3.ZERO);
            state.forward = getDirection(tag, "forward", new Vec3(0.0, 0.0, 1.0));
            state.right = getDirection(tag, "right", new Vec3(-1.0, 0.0, 0.0));
            float damage = tag.getFloat("damage_snapshot");
            float critical = tag.getFloat("critical_multiplier");
            state.triggerPreDefenseDamage =
                    Float.isFinite(damage) && damage >= 0.0F ? damage : 0.0F;
            state.stageCriticalMultiplier =
                    Float.isFinite(critical) && critical >= 1.0F ? critical : 1.0F;
            long[] targets = tag.getLongArray("hit_targets");
            int limit = Math.min(targets.length - targets.length % 2, MAX_SAVED_TARGETS * 2);
            for (int index = 0; index < limit; index += 2) {
                state.hitTargets.add(new UUID(targets[index], targets[index + 1]));
            }
            // Never sweep from a stale pre-save blade pose on the first tick after loading.
            state.previousBlade = null;
            return state;
        } catch (RuntimeException ignored) {
            return new FerinPlayerState();
        }
    }

    private Vec3 pointToWorld(FerinBladeTrajectory.Point point) {
        return companionAnchor()
                .add(right.scale(point.right()))
                .add(0.0, point.up(), 0.0)
                .add(forward.scale(point.forward()));
    }

    private static void putVec(CompoundTag tag, String name, Vec3 vector) {
        CompoundTag value = new CompoundTag();
        value.putDouble("x", vector.x);
        value.putDouble("y", vector.y);
        value.putDouble("z", vector.z);
        tag.put(name, value);
    }

    private static Vec3 getFiniteVec(CompoundTag tag, String name, Vec3 fallback) {
        if (!tag.contains(name, Tag.TAG_COMPOUND)) {
            return fallback;
        }
        CompoundTag value = tag.getCompound(name);
        double x = value.getDouble("x");
        double y = value.getDouble("y");
        double z = value.getDouble("z");
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
                ? new Vec3(x, y, z)
                : fallback;
    }

    private static Vec3 getDirection(CompoundTag tag, String name, Vec3 fallback) {
        Vec3 direction = getFiniteVec(tag, name, fallback);
        double lengthSquared = direction.lengthSqr();
        return lengthSquared >= 0.5 && lengthSquared <= 1.5 ? direction : fallback;
    }
}
