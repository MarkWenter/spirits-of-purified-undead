package dev.purifiedundead.progress;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.nbt.CompoundTag;

/** Versioned, player-owned acquisition state for one-time warrior rewards. */
public final class WarriorProgress {
    private static final int FORMAT_VERSION = 10;

    private boolean ferinObtained;
    private boolean ferinPending;
    private boolean grothObtained;
    private boolean grothPending;
    private boolean juliusObtained;
    private boolean juliusPending;
    private boolean guardiansObtained;
    private boolean guardiansPending;
    private boolean ulvObtained;
    private boolean ulvPending;
    private boolean eleineObtained;
    private boolean eleinePending;
    private int eleineDrownedKills;
    private boolean hoenirObtained;
    private boolean hoenirPending;
    private boolean fadenObtained;
    private boolean fadenPending;
    private boolean contractObtained;
    private boolean contractPending;
    private int talismanLevel;

    public enum ClaimResult {
        DELIVERED,
        PENDING,
        ALREADY_OBTAINED
    }

    public boolean ferinObtained() {
        return ferinObtained;
    }

    public boolean ferinPending() {
        return ferinPending;
    }

    public boolean grothObtained() {
        return grothObtained;
    }

    public boolean grothPending() {
        return grothPending;
    }

    public boolean juliusObtained() {
        return juliusObtained;
    }

    public boolean juliusPending() {
        return juliusPending;
    }

    public boolean guardiansObtained() {
        return guardiansObtained;
    }

    public boolean guardiansPending() {
        return guardiansPending;
    }

    public boolean ulvObtained() {
        return ulvObtained;
    }

    public boolean ulvPending() {
        return ulvPending;
    }

    public boolean eleineObtained() {
        return eleineObtained;
    }

    public boolean eleinePending() {
        return eleinePending;
    }

    public int eleineDrownedKills() {
        return eleineDrownedKills;
    }

    public boolean hoenirObtained() {
        return hoenirObtained;
    }

    public boolean hoenirPending() {
        return hoenirPending;
    }

    public boolean fadenObtained() {
        return fadenObtained;
    }

    public boolean fadenPending() {
        return fadenPending;
    }

    public boolean contractObtained() {
        return contractObtained;
    }

    public boolean contractPending() {
        return contractPending;
    }

    public int talismanLevel() {
        return talismanLevel;
    }

    public boolean upgradeTalisman() {
        if (talismanLevel >= WhiteWitchTalisman.maxLevel()) {
            return false;
        }
        talismanLevel++;
        return true;
    }

    public ClaimResult claimFerin(boolean delivered) {
        if (ferinObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        ferinObtained = true;
        ferinPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingFerin(boolean delivered) {
        if (!ferinObtained || !ferinPending || !delivered) {
            return false;
        }
        ferinPending = false;
        return true;
    }

    public ClaimResult claimGroth(boolean delivered) {
        if (grothObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        grothObtained = true;
        grothPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingGroth(boolean delivered) {
        if (!grothObtained || !grothPending || !delivered) {
            return false;
        }
        grothPending = false;
        return true;
    }

    public ClaimResult claimJulius(boolean delivered) {
        if (juliusObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        juliusObtained = true;
        juliusPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingJulius(boolean delivered) {
        if (!juliusObtained || !juliusPending || !delivered) {
            return false;
        }
        juliusPending = false;
        return true;
    }

    public ClaimResult claimGuardians(boolean delivered) {
        if (guardiansObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        guardiansObtained = true;
        guardiansPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingGuardians(boolean delivered) {
        if (!guardiansObtained || !guardiansPending || !delivered) {
            return false;
        }
        guardiansPending = false;
        return true;
    }

    public ClaimResult claimUlv(boolean delivered) {
        if (ulvObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        ulvObtained = true;
        ulvPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingUlv(boolean delivered) {
        if (!ulvObtained || !ulvPending || !delivered) {
            return false;
        }
        ulvPending = false;
        return true;
    }

    public int recordEleineDrownedKill() {
        int required = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineDrownedKillsRequired);
        if (!eleineObtained && eleineDrownedKills < required) {
            eleineDrownedKills++;
        }
        return eleineDrownedKills;
    }

    public ClaimResult claimEleine(boolean delivered) {
        if (eleineObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        eleineObtained = true;
        eleinePending = !delivered;
        eleineDrownedKills = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineDrownedKillsRequired);
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingEleine(boolean delivered) {
        if (!eleineObtained || !eleinePending || !delivered) {
            return false;
        }
        eleinePending = false;
        return true;
    }

    public ClaimResult claimHoenir(boolean delivered) {
        if (hoenirObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        hoenirObtained = true;
        hoenirPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingHoenir(boolean delivered) {
        if (!hoenirObtained || !hoenirPending || !delivered) {
            return false;
        }
        hoenirPending = false;
        return true;
    }

    public ClaimResult claimFaden(boolean delivered) {
        if (fadenObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        fadenObtained = true;
        fadenPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingFaden(boolean delivered) {
        if (!fadenObtained || !fadenPending || !delivered) {
            return false;
        }
        fadenPending = false;
        return true;
    }

    public ClaimResult claimContract(boolean delivered) {
        if (contractObtained) {
            return ClaimResult.ALREADY_OBTAINED;
        }
        contractObtained = true;
        contractPending = !delivered;
        return delivered ? ClaimResult.DELIVERED : ClaimResult.PENDING;
    }

    public boolean resolvePendingContract(boolean delivered) {
        if (!contractObtained || !contractPending || !delivered) {
            return false;
        }
        contractPending = false;
        return true;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", FORMAT_VERSION);
        tag.putBoolean("ferin_obtained", ferinObtained);
        tag.putBoolean("ferin_pending", ferinPending);
        tag.putBoolean("groth_obtained", grothObtained);
        tag.putBoolean("groth_pending", grothPending);
        tag.putBoolean("julius_obtained", juliusObtained);
        tag.putBoolean("julius_pending", juliusPending);
        tag.putBoolean("guardians_obtained", guardiansObtained);
        tag.putBoolean("guardians_pending", guardiansPending);
        tag.putBoolean("ulv_obtained", ulvObtained);
        tag.putBoolean("ulv_pending", ulvPending);
        tag.putBoolean("eleine_obtained", eleineObtained);
        tag.putBoolean("eleine_pending", eleinePending);
        tag.putInt("eleine_drowned_kills", eleineDrownedKills);
        tag.putBoolean("hoenir_obtained", hoenirObtained);
        tag.putBoolean("hoenir_pending", hoenirPending);
        tag.putBoolean("faden_obtained", fadenObtained);
        tag.putBoolean("faden_pending", fadenPending);
        tag.putBoolean("contract_obtained", contractObtained);
        tag.putBoolean("contract_pending", contractPending);
        tag.putInt("talisman_level", talismanLevel);
        return tag;
    }

    public static WarriorProgress load(CompoundTag tag) {
        WarriorProgress progress = new WarriorProgress();
        if (tag.getInt("version") != FORMAT_VERSION) {
            // Permanent grants fail closed across future/downgraded formats: preserve
            // any explicit acquisition bit so an unknown version cannot duplicate a reward.
            progress.ferinObtained = tag.getBoolean("ferin_obtained");
            progress.ferinPending = progress.ferinObtained && tag.getBoolean("ferin_pending");
            progress.grothObtained = tag.getBoolean("groth_obtained");
            progress.grothPending = progress.grothObtained && tag.getBoolean("groth_pending");
            progress.juliusObtained = tag.getBoolean("julius_obtained");
            progress.juliusPending = progress.juliusObtained && tag.getBoolean("julius_pending");
            progress.guardiansObtained = tag.getBoolean("guardians_obtained");
            progress.guardiansPending = progress.guardiansObtained && tag.getBoolean("guardians_pending");
            progress.ulvObtained = tag.getBoolean("ulv_obtained");
            progress.ulvPending = progress.ulvObtained && tag.getBoolean("ulv_pending");
            progress.eleineObtained = tag.getBoolean("eleine_obtained");
            progress.eleinePending = progress.eleineObtained && tag.getBoolean("eleine_pending");
            int required = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineDrownedKillsRequired);
            progress.eleineDrownedKills = Math.max(0, Math.min(required, tag.getInt("eleine_drowned_kills")));
            if (progress.eleineObtained) {
                progress.eleineDrownedKills = required;
            }
            progress.hoenirObtained = tag.getBoolean("hoenir_obtained");
            progress.hoenirPending = progress.hoenirObtained && tag.getBoolean("hoenir_pending");
            progress.fadenObtained = tag.getBoolean("faden_obtained");
            progress.fadenPending = progress.fadenObtained && tag.getBoolean("faden_pending");
            progress.contractObtained = tag.getBoolean("contract_obtained");
            progress.contractPending = progress.contractObtained && tag.getBoolean("contract_pending");
            progress.talismanLevel = WhiteWitchTalisman.clampLevel(tag.getInt("talisman_level"));
            return progress;
        }
        progress.ferinObtained = tag.getBoolean("ferin_obtained");
        progress.ferinPending = progress.ferinObtained && tag.getBoolean("ferin_pending");
        progress.grothObtained = tag.getBoolean("groth_obtained");
        progress.grothPending = progress.grothObtained && tag.getBoolean("groth_pending");
        progress.juliusObtained = tag.getBoolean("julius_obtained");
        progress.juliusPending = progress.juliusObtained && tag.getBoolean("julius_pending");
        progress.guardiansObtained = tag.getBoolean("guardians_obtained");
        progress.guardiansPending = progress.guardiansObtained && tag.getBoolean("guardians_pending");
        progress.ulvObtained = tag.getBoolean("ulv_obtained");
        progress.ulvPending = progress.ulvObtained && tag.getBoolean("ulv_pending");
        progress.eleineObtained = tag.getBoolean("eleine_obtained");
        progress.eleinePending = progress.eleineObtained && tag.getBoolean("eleine_pending");
        int required = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineDrownedKillsRequired);
        progress.eleineDrownedKills = Math.max(0, Math.min(required, tag.getInt("eleine_drowned_kills")));
        if (progress.eleineObtained) {
            progress.eleineDrownedKills = required;
        }
        progress.hoenirObtained = tag.getBoolean("hoenir_obtained");
        progress.hoenirPending = progress.hoenirObtained && tag.getBoolean("hoenir_pending");
        progress.fadenObtained = tag.getBoolean("faden_obtained");
        progress.fadenPending = progress.fadenObtained && tag.getBoolean("faden_pending");
        progress.contractObtained = tag.getBoolean("contract_obtained");
        progress.contractPending = progress.contractObtained && tag.getBoolean("contract_pending");
        progress.talismanLevel = WhiteWitchTalisman.clampLevel(tag.getInt("talisman_level"));
        return progress;
    }
}
