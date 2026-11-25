package dev.purifiedundead.progress;

import dev.purifiedundead.entity.BlightedGolemEntity;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Grants Groth to the qualifying final killer of a blighted golem. */
public final class GrothAcquisitionEvents {
    @SubscribeEvent
    public void onBlightedGolemDeath(LivingDeathEvent event) {
        if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothAcquisitionEnabled)
                && event.getEntity() instanceof BlightedGolemEntity
                && event.getSource().getEntity() instanceof ServerPlayer player
                && ContractProgressService.hasContract(player)) {
            WarriorRewardService.onGrothDefeated(player);
        }
    }
}
