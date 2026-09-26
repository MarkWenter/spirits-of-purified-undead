package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Marks an evoker as the Blighted King and grants Julius to its qualifying final killer. */
public final class JuliusAcquisitionEvents {
    private static final String BLIGHTED_KING_KEY = "purified_undead:blighted_king";

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.juliusAcquisitionEnabled)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof Evoker evoker)
                || evoker.getPersistentData().getBoolean(BLIGHTED_KING_KEY)
                || !event.getItemStack().is(ModItems.BLIGHTED_SPIRIT.get())
                || !(event.getLevel() instanceof ServerLevel level)
                || !ContractProgressService.hasContract(player)) {
            return;
        }

        evoker.getPersistentData().putBoolean(BLIGHTED_KING_KEY, true);
        evoker.setPersistenceRequired();
        if (!evoker.hasCustomName()) {
            evoker.setCustomName(Component.translatable("entity.purified_undead.blighted_king"));
            evoker.setCustomNameVisible(true);
        }
        if (!player.getAbilities().instabuild) {
            event.getItemStack().shrink(1);
        }
        level.sendParticles(ParticleTypes.SOUL, evoker.getX(), evoker.getY() + 1.0D, evoker.getZ(),
                24, 0.55D, 0.8D, 0.55D, 0.035D);
        player.displayClientMessage(Component.translatable("message.purified_undead.julius.transformed"), false);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlightedKingDeath(LivingDeathEvent event) {
        if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.juliusAcquisitionEnabled)
                && event.getEntity() instanceof Evoker evoker
                && evoker.getPersistentData().getBoolean(BLIGHTED_KING_KEY)
                && event.getSource().getEntity() instanceof ServerPlayer player
                && ContractProgressService.hasContract(player)) {
            WarriorRewardService.onJuliusDefeated(player);
        }
    }
}
