package dev.purifiedundead.progress;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarriorProgressTest {
    @Test
    void deliveredRewardIsPermanentAndCannotBeClaimedAgain() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.DELIVERED, progress.claimFerin(true));

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.ferinObtained());
        assertFalse(restored.ferinPending());
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimFerin(true));
    }

    @Test
    void fullInventoryCreatesReliablePendingReward() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimFerin(false));

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.ferinObtained());
        assertTrue(restored.ferinPending());
        assertFalse(restored.resolvePendingFerin(false));
        assertTrue(restored.resolvePendingFerin(true));
        assertFalse(restored.ferinPending());
    }

    @Test
    void unknownVersionPreservesExplicitPermanentGrant() {
        CompoundTag unknown = new CompoundTag();
        unknown.putInt("version", 99);
        unknown.putBoolean("ferin_obtained", true);
        unknown.putBoolean("ferin_pending", true);

        WarriorProgress restored = WarriorProgress.load(unknown);
        assertTrue(restored.ferinObtained());
        assertTrue(restored.ferinPending());

        CompoundTag emptyUnknown = new CompoundTag();
        emptyUnknown.putInt("version", 99);
        assertFalse(WarriorProgress.load(emptyUnknown).ferinObtained());
    }

    @Test
    void talismanUpgradeIsPermanentAndCapped() {
        WarriorProgress progress = new WarriorProgress();
        for (int level = 1; level <= WhiteWitchTalisman.MAX_LEVEL; level++) {
            assertTrue(progress.upgradeTalisman());
            assertEquals(level, progress.talismanLevel());
        }
        assertFalse(progress.upgradeTalisman());
        assertEquals(
                WhiteWitchTalisman.MAX_LEVEL,
                WarriorProgress.load(progress.save()).talismanLevel());
    }

    @Test
    void versionOneProgressMigratesWithLevelZero() {
        CompoundTag old = new CompoundTag();
        old.putInt("version", 1);
        old.putBoolean("ferin_obtained", true);
        WarriorProgress restored = WarriorProgress.load(old);
        assertTrue(restored.ferinObtained());
        assertEquals(0, restored.talismanLevel());
    }

    @Test
    void grothRewardUsesIndependentPermanentAndPendingState() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimGroth(false));
        assertFalse(progress.ferinObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.grothObtained());
        assertTrue(restored.grothPending());
        assertTrue(restored.resolvePendingGroth(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimGroth(true));
    }

    @Test
    void juliusRewardUsesIndependentPermanentAndPendingState() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimJulius(false));
        assertFalse(progress.grothObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.juliusObtained());
        assertTrue(restored.juliusPending());
        assertTrue(restored.resolvePendingJulius(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimJulius(true));
    }

    @Test
    void guardianRewardUsesIndependentPermanentAndPendingState() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimGuardians(false));
        assertFalse(progress.juliusObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.guardiansObtained());
        assertTrue(restored.guardiansPending());
        assertTrue(restored.resolvePendingGuardians(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimGuardians(true));
    }

    @Test
    void ulvRewardUsesIndependentPermanentAndPendingState() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimUlv(false));
        assertFalse(progress.guardiansObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.ulvObtained());
        assertTrue(restored.ulvPending());
        assertTrue(restored.resolvePendingUlv(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimUlv(true));
    }

    @Test
    void eleineKillProgressPersistsBeforeIndependentPendingReward() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(1, progress.recordEleineDrownedKill());
        assertEquals(2, progress.recordEleineDrownedKill());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertEquals(2, restored.eleineDrownedKills());
        assertFalse(restored.eleineObtained());
        assertEquals(3, restored.recordEleineDrownedKill());
        assertEquals(WarriorProgress.ClaimResult.PENDING, restored.claimEleine(false));

        WarriorProgress pending = WarriorProgress.load(restored.save());
        assertTrue(pending.eleineObtained());
        assertTrue(pending.eleinePending());
        assertEquals(3, pending.eleineDrownedKills());
        assertTrue(pending.resolvePendingEleine(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, pending.claimEleine(true));
    }

    @Test
    void hoenirRewardUsesIndependentPermanentAndPendingState() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimHoenir(false));
        assertFalse(progress.eleineObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.hoenirObtained());
        assertTrue(restored.hoenirPending());
        assertTrue(restored.resolvePendingHoenir(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimHoenir(true));
    }

    @Test
    void fadenRewardUsesIndependentPermanentAndPendingState() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimFaden(false));
        assertFalse(progress.hoenirObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.fadenObtained());
        assertTrue(restored.fadenPending());
        assertTrue(restored.resolvePendingFaden(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimFaden(true));
    }

    @Test
    void contractRewardIsOneTimeAndCanWaitForInventorySpace() {
        WarriorProgress progress = new WarriorProgress();
        assertEquals(WarriorProgress.ClaimResult.PENDING, progress.claimContract(false));
        assertFalse(progress.ferinObtained());

        WarriorProgress restored = WarriorProgress.load(progress.save());
        assertTrue(restored.contractObtained());
        assertTrue(restored.contractPending());
        assertTrue(restored.resolvePendingContract(true));
        assertEquals(WarriorProgress.ClaimResult.ALREADY_OBTAINED, restored.claimContract(true));
    }
}
