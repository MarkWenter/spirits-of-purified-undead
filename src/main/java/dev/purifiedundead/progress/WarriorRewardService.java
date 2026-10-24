package dev.purifiedundead.progress;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-only delivery for the first-contract Ferin reward. */
public final class WarriorRewardService {
    private WarriorRewardService() {}

    public static void onContractEquipped(ServerPlayer player) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinAcquisitionEnabled)) {
            return;
        }
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.ferinObtained()) {
            retryPendingRewards(player, progress);
            return;
        }

        boolean delivered = insertFerin(player);
        WarriorProgress.ClaimResult result = progress.claimFerin(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.ferin_reward.delivered"
                                : "message.purified_undead.ferin_reward.pending"),
                false);
    }

    public static void retryPending(ServerPlayer player) {
        retryPendingRewards(player, WarriorProgressStorage.load(player));
    }

    private static void retryPendingRewards(ServerPlayer player, WarriorProgress progress) {
        if (progress.contractPending() && insert(player, ModItems.ANCIENT_CONTRACT.get())) {
            progress.resolvePendingContract(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.contract_reward.delivered_pending"),
                    false);
        }
        if (progress.ferinPending() && insert(player, ModItems.FERIN_WARRIOR.get())) {
            progress.resolvePendingFerin(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.ferin_reward.delivered_pending"),
                    false);
        }
        if (progress.grothPending() && insert(player, ModItems.GROTH_WARRIOR.get())) {
            progress.resolvePendingGroth(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.groth_reward.delivered_pending"),
                    false);
        }
        if (progress.juliusPending() && insert(player, ModItems.JULIUS_WARRIOR.get())) {
            progress.resolvePendingJulius(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.julius_reward.delivered_pending"),
                    false);
        }
        if (progress.guardiansPending() && insert(player, ModItems.GUARDIAN_WARRIORS.get())) {
            progress.resolvePendingGuardians(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.guardians_reward.delivered_pending"),
                    false);
        }
        if (progress.ulvPending() && insert(player, ModItems.ULV_WARRIOR.get())) {
            progress.resolvePendingUlv(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable("message.purified_undead.ulv_reward.delivered_pending"),
                    false);
        }
        if (progress.eleinePending() && insert(player, ModItems.ELEINE_WARRIOR.get())) {
            progress.resolvePendingEleine(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.eleine_reward.delivered_pending"),
                    false);
        }
        if (progress.hoenirPending() && insert(player, ModItems.HOENIR_WARRIOR.get())) {
            progress.resolvePendingHoenir(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.hoenir_reward.delivered_pending"),
                    false);
        }
        if (progress.fadenPending() && insert(player, ModItems.FADEN_WARRIOR.get())) {
            progress.resolvePendingFaden(true);
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.faden_reward.delivered_pending"),
                    false);
        }
    }

    public static void onGrothDefeated(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.grothObtained()) {
            return;
        }
        boolean delivered = insert(player, ModItems.GROTH_WARRIOR.get());
        WarriorProgress.ClaimResult result = progress.claimGroth(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.groth_reward.delivered"
                                : "message.purified_undead.groth_reward.pending"),
                false);
    }

    public static void onJuliusDefeated(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.juliusObtained()) {
            return;
        }
        boolean delivered = insert(player, ModItems.JULIUS_WARRIOR.get());
        WarriorProgress.ClaimResult result = progress.claimJulius(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.julius_reward.delivered"
                                : "message.purified_undead.julius_reward.pending"),
                false);
    }

    public static boolean grantGuardians(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.guardiansObtained()) {
            return false;
        }
        boolean delivered = insert(player, ModItems.GUARDIAN_WARRIORS.get());
        WarriorProgress.ClaimResult result = progress.claimGuardians(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.guardians_reward.delivered"
                                : "message.purified_undead.guardians_reward.pending"),
                false);
        return true;
    }

    public static boolean grantUlv(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.ulvObtained()) {
            return false;
        }
        boolean delivered = insert(player, ModItems.ULV_WARRIOR.get());
        WarriorProgress.ClaimResult result = progress.claimUlv(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.ulv_reward.delivered"
                                : "message.purified_undead.ulv_reward.pending"),
                false);
        return true;
    }

    public static boolean recordEleineDrownedKill(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.eleineObtained()) {
            return false;
        }
        int kills = progress.recordEleineDrownedKill();
        int required =
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineDrownedKillsRequired);
        if (kills < required) {
            WarriorProgressStorage.save(player, progress);
            message(
                    player,
                    Component.translatable(
                            "message.purified_undead.eleine_reward.progress", kills, required),
                    false);
            return false;
        }
        boolean delivered = insert(player, ModItems.ELEINE_WARRIOR.get());
        WarriorProgress.ClaimResult result = progress.claimEleine(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.eleine_reward.delivered"
                                : "message.purified_undead.eleine_reward.pending"),
                false);
        return true;
    }

    public static boolean grantHoenir(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.hoenirObtained()) {
            return false;
        }
        boolean delivered = insert(player, ModItems.HOENIR_WARRIOR.get());
        WarriorProgress.ClaimResult result = progress.claimHoenir(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.hoenir_reward.delivered"
                                : "message.purified_undead.hoenir_reward.pending"),
                false);
        return true;
    }

    public static boolean grantFaden(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.fadenObtained()) {
            return false;
        }
        boolean delivered = insert(player, ModItems.FADEN_WARRIOR.get());
        WarriorProgress.ClaimResult result = progress.claimFaden(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.faden_reward.delivered"
                                : "message.purified_undead.faden_reward.pending"),
                false);
        return true;
    }

    public static boolean grantContract(ServerPlayer player) {
        WarriorProgress progress = WarriorProgressStorage.load(player);
        if (progress.contractObtained()) {
            return false;
        }
        boolean delivered = insert(player, ModItems.ANCIENT_CONTRACT.get());
        WarriorProgress.ClaimResult result = progress.claimContract(delivered);
        WarriorProgressStorage.save(player, progress);
        message(
                player,
                Component.translatable(
                        result == WarriorProgress.ClaimResult.DELIVERED
                                ? "message.purified_undead.contract_reward.delivered"
                                : "message.purified_undead.contract_reward.pending"),
                false);
        return true;
    }

    public static void copyProgress(ServerPlayer original, ServerPlayer replacement) {
        WarriorProgressStorage.copy(original, replacement);
        RewardSounds.copy(original, replacement);
    }

    private static void message(ServerPlayer player, Component text, boolean actionBar) {
        dev.purifiedundead.util.FailureIsolation.run(
                "reward message", () -> player.displayClientMessage(text, actionBar));
    }

    private static boolean insertFerin(ServerPlayer player) {
        return insert(player, ModItems.FERIN_WARRIOR.get());
    }

    public static boolean insert(ServerPlayer player, net.minecraft.world.item.Item item) {
        // Preserve the pending flag until a live replacement player can receive the item.
        if (!player.isAlive()) return false;
        ItemStack reward = new ItemStack(item);
        boolean inserted;
        try {
            inserted = InventoryTransfer.insert(player.getInventory(), reward, () -> {});
        } catch (RuntimeException | LinkageError failure) {
            dev.purifiedundead.util.FailureIsolation.report(
                    "reward insertion rolled back; delivery pending", failure);
            return false;
        }
        if (inserted) {
            RewardSounds.requestSync(player);
            dev.purifiedundead.util.FailureIsolation.run(
                    "reward sound queue", () -> RewardSounds.onDelivered(player, item));
        }
        return inserted;
    }
}
