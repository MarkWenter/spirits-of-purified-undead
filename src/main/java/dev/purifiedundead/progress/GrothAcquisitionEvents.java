package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.entity.BlightedGolemEntity;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Converts iron golems and grants Groth to the qualifying final killer exactly once. */
public final class GrothAcquisitionEvents {
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothAcquisitionEnabled)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof IronGolem original)
                || original instanceof BlightedGolemEntity
                || !event.getItemStack().is(ModItems.BLIGHTED_SPIRIT.get())
                || !(event.getLevel() instanceof ServerLevel level)
                || !ContractProgressService.hasContract(player)) {
            return;
        }

        var converted = BlightedConversions.convert(original, level);
        if (converted == null) return;
        if (!player.getAbilities().instabuild) {
            event.getItemStack().shrink(1);
        }
        player.displayClientMessage(
                Component.translatable("message.purified_undead.groth.transformed"), false);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

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
