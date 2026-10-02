package dev.purifiedundead.combat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FerinPlayerStateTest {
    private static final FerinComboTimings TIMINGS = new FerinComboTimings(40, 10, 24, 16, 4);

    @Test
    void roundTripsComboCooldownStageContextAndHitTargets() {
        var state = new FerinPlayerState();
        state.combo.onQualifyingHit(120, 3, TIMINGS);
        assertTrue(state.attackTickGate.accept(120));
        state.captureStage(new Vec3(10.5, 64.0, -3.25), 90.0F, 12.0F, 1.5F);
        UUID target = UUID.randomUUID();
        state.hitTargets.add(target);
        state.previousBlade = new FerinSweptBlade.WorldPose(Vec3.ZERO, new Vec3(1, 0, 0), 0.2);

        var restored = FerinPlayerState.load(state.save());
        assertEquals(1, restored.combo.stage());
        assertEquals(120, restored.combo.stageStartedAt());
        assertEquals(160, restored.combo.cooldownUntil());
        assertFalse(restored.attackTickGate.accept(120));
        assertEquals(new Vec3(10.5, 64.0, -3.25), restored.origin);
        assertEquals(12.0F, restored.triggerPreDefenseDamage);
        assertEquals(1.5F, restored.stageCriticalMultiplier);
        assertTrue(restored.hitTargets.contains(target));
        assertNull(restored.previousBlade, "A loaded state must not sweep from a stale pre-save pose");
    }

    @Test
    void rejectsUnknownOrCorruptPersistentDataSafely() {
        CompoundTag unknown = new CompoundTag();
        unknown.putInt("version", 99);
        assertEquals(0, FerinPlayerState.load(unknown).combo.stage());

        CompoundTag corrupt = new CompoundTag();
        corrupt.putInt("version", 1);
        corrupt.putInt("stage", 99);
        assertEquals(0, FerinPlayerState.load(corrupt).combo.stage());

        CompoundTag missingDirections = new CompoundTag();
        missingDirections.putInt("version", 1);
        assertEquals(new Vec3(0, 0, 1), FerinPlayerState.load(missingDirections).forward);
    }

    @Test
    void capsTheSavedPerStageTargetSet() {
        var state = new FerinPlayerState();
        for (int index = 0; index < 300; index++) {
            state.hitTargets.add(UUID.randomUUID());
        }
        assertEquals(256, FerinPlayerState.load(state.save()).hitTargets.size());
    }

    @Test
    void placesCompanionAndBladeAtTheCapturedLeftFrontAnchor() {
        var state = new FerinPlayerState();
        state.captureStage(new Vec3(10.0, 64.0, 20.0), 0.0F, 8.0F, 1.0F);

        Vec3 offset = state.companionAnchor().subtract(state.origin);
        assertEquals(FerinPlayerState.COMPANION_LEFT_OFFSET, offset.dot(state.right), 0.0001D);
        assertEquals(FerinPlayerState.COMPANION_FORWARD_OFFSET, offset.dot(state.forward), 0.0001D);
        assertEquals(0.0F, state.capturedYawDegrees(), 0.0001F);
        assertEquals(180.0F, state.ferinVisualYawDegrees(), 0.0001F,
                "The model front must face combat-forward rather than back toward its owner");

        FerinBladeTrajectory.BladePose local = FerinBladeTrajectory.sample(1, 2).orElseThrow();
        FerinSweptBlade.WorldPose world = state.toWorld(local);
        Vec3 rootFromAnchor = world.root().subtract(state.companionAnchor());
        assertEquals(local.root().right(), rootFromAnchor.dot(state.right), 0.0001D);
        assertEquals(local.root().forward(), rootFromAnchor.dot(state.forward), 0.0001D);

        state.captureStage(Vec3.ZERO, 90.0F, 8.0F, 1.0F);
        assertEquals(90.0F, state.capturedYawDegrees(), 0.0001F);
        assertEquals(270.0F, state.ferinVisualYawDegrees(), 0.0001F);
        Vec3 rotatedOffset = state.companionAnchor();
        assertEquals(FerinPlayerState.COMPANION_LEFT_OFFSET, rotatedOffset.dot(state.right), 0.0001D);
        assertEquals(FerinPlayerState.COMPANION_FORWARD_OFFSET, rotatedOffset.dot(state.forward), 0.0001D);
    }

    @Test
    void aimsTheSummonAtTheActuallyHitTargetAndFallsBackForCoincidentPositions() {
        var state = new FerinPlayerState();
        state.captureStageTowards(new Vec3(10.0, 64.0, 10.0), new Vec3(13.0, 70.0, 14.0),
                180.0F, 8.0F, 1.0F);
        assertEquals(0.6D, state.forward.x, 0.0001D);
        assertEquals(0.0D, state.forward.y, 0.0001D);
        assertEquals(0.8D, state.forward.z, 0.0001D);

        state.captureStageTowards(Vec3.ZERO, Vec3.ZERO, 90.0F, 8.0F, 1.0F);
        assertEquals(-1.0D, state.forward.x, 0.0001D);
        assertEquals(0.0D, state.forward.z, 0.0001D);
    }
    @Test
    void cancellationClearsQueuedContinuationAndTargets() {
        var state = new FerinPlayerState();
        state.combo.onQualifyingHit(100,3,TIMINGS);
        state.continuation.request(1,100);
        state.continuationTarget = new Vec3(10,64,10);
        state.hitTargets.add(UUID.randomUUID());
        state.cancelActive();
        assertFalse(state.continuation.pending(1,100));
        assertNull(state.continuationTarget);
        assertTrue(state.hitTargets.isEmpty());
        assertEquals(140,state.combo.cooldownUntil());
        assertEquals(0,FerinPlayerState.load(state.save()).combo.stage());
    }
}
