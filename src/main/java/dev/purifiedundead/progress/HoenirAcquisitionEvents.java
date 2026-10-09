package dev.purifiedundead.progress;

import dev.purifiedundead.combat.HoenirModel;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Grants Hoenir when a player suffering three distinct harmful effects kills a zombie. */
public final class HoenirAcquisitionEvents {
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.hoenirAcquisitionEnabled)
                || !(event.getEntity() instanceof Zombie)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long harmfulEffects =
                player.getActiveEffects().stream()
                        .filter(
                                effect ->
                                        effect.getEffect().getCategory()
                                                == MobEffectCategory.HARMFUL)
                        .map(effect -> effect.getEffect())
                        .distinct()
                        .count();
        if (harmfulEffects
                >= PurifiedUndeadConfig.get(
                        PurifiedUndeadConfig.VALUES.hoenirNegativeEffectsRequired)) {
            WarriorRewardService.grantHoenir(player);
        }
    }
}
