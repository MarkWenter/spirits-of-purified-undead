package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Tracks three player-owned drowned kills while the Tired Heart is held in the offhand. */
public final class EleineAcquisitionEvents {
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineAcquisitionEnabled)
                || !(event.getEntity() instanceof Drowned)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.getOffhandItem().is(ModItems.TIRED_HEART.get())) {
            return;
        }
        if (WarriorRewardService.recordEleineDrownedKill(player) && !player.getAbilities().instabuild) {
            player.getOffhandItem().shrink(1);
        }
    }
}
