package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import top.theillusivec4.curios.api.CuriosApi;

/** Recovery hooks for reward delivery and player entity replacement. */
public final class ContractProgressEvents {
    private static final String BLIGHTED_DEATH_KEY = "purified_undead:blighted_death";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        RewardSounds.tick(player);
        if (player.tickCount % 20 != 0) return;
        WarriorRewardService.retryPending(player);
        boolean contractEquipped = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(ModItems.ANCIENT_CONTRACT.get())).orElse(false);
        if (contractEquipped) {
            WarriorRewardService.onContractEquipped(player);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original && event.getEntity() instanceof ServerPlayer replacement) {
            WarriorRewardService.copyProgress(original, replacement);
            if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.contractAcquisitionEnabled)
                    && event.isWasDeath()
                    && original.getPersistentData().getBoolean(BLIGHTED_DEATH_KEY)) {
                original.getPersistentData().remove(BLIGHTED_DEATH_KEY);
                WarriorRewardService.grantContract(replacement);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.contractAcquisitionEnabled)
                && event.getEntity() instanceof ServerPlayer player
                && player.hasEffect(dev.purifiedundead.content.ModEffects.BLIGHTED_TRANSFORMATION.get())) {
            player.getPersistentData().putBoolean(BLIGHTED_DEATH_KEY, true);
        }
    }
}
