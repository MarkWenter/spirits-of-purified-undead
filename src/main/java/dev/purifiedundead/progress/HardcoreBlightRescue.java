package dev.purifiedundead.progress;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Consumes the ritual state instead of a hardcore player's life. */
public final class HardcoreBlightRescue {
    public static boolean onSaved(ServerPlayer player) {
        if (!player.server.isHardcore()
                || !PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.contractAcquisitionEnabled)
                || !player.hasEffect(ModEffects.BLIGHTED_TRANSFORMATION.get())) return false;
        player.removeEffect(ModEffects.BLIGHTED_TRANSFORMATION.get());
        player.getPersistentData().remove("purified_undead:blighted_death");
        // A committed rescue must restore life before handing items to the live-player delivery path.
        if (player.getHealth() <= 0) player.setHealth(1.0F);
        WarriorRewardService.grantContract(player);
        return true;
    }

    public static boolean rescue(ServerPlayer player) {
        if (!onSaved(player)) return false;
        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.invulnerableTime = 20;
        player.level().broadcastEntityEvent(player, (byte) 35);
        return true;
    }

    private HardcoreBlightRescue() {}
}
