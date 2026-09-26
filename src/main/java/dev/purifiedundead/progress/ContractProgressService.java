package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerPlayer;
import top.theillusivec4.curios.api.CuriosApi;

/** Server-authoritative access to contract progression stored on the player. */
public final class ContractProgressService {
    private ContractProgressService() {
    }

    public static boolean hasContract(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(ModItems.ANCIENT_CONTRACT.get())).orElse(false);
    }

    public static int talismanLevel(ServerPlayer player) {
        return WarriorProgressStorage.load(player).talismanLevel();
    }

    public static int ferinMaxStages(ServerPlayer player) {
        return WhiteWitchTalisman.ferinMaxStages(talismanLevel(player));
    }

    public static float incomingDamageMultiplier(ServerPlayer player) {
        return WhiteWitchTalisman.incomingDamageMultiplier(talismanLevel(player));
    }

    public static boolean upgradeTalisman(ServerPlayer player) {
        if (!hasContract(player)) {
            return false;
        }
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (!progress.upgradeTalisman()) {
            return false;
        }
        WarriorProgressStorage.save(player, progress);
        RewardSounds.onUpgrade(player, progress.talismanLevel());
        return true;
    }
}
