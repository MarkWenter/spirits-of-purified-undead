package dev.purifiedundead.progress;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/** Attributes a completed zombie-villager cure to the vanilla conversion starter. */
public final class FadenAcquisitionEvents {
    @SubscribeEvent
    public void onConversion(LivingConversionEvent.Post event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenAcquisitionEnabled)
                || !(event.getEntity() instanceof ZombieVillager zombie)
                || event.getOutcome().getType() != EntityType.VILLAGER) {
            return;
        }
        CompoundTag data = new CompoundTag();
        zombie.addAdditionalSaveData(data);
        if (!data.hasUUID("ConversionPlayer")) {
            return;
        }
        UUID ownerId = data.getUUID("ConversionPlayer");
        ServerPlayer owner =
                zombie.getServer() == null
                        ? null
                        : zombie.getServer().getPlayerList().getPlayer(ownerId);
        if (owner != null) {
            WarriorRewardService.grantFaden(owner);
        } else if (zombie.getServer() != null) {
            FadenCureLedger.get(zombie.getServer()).add(ownerId);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenAcquisitionEnabled)
                && event.getEntity() instanceof ServerPlayer player
                && FadenCureLedger.get(player.server).take(player.getUUID())) {
            WarriorRewardService.grantFaden(player);
        }
    }
}
