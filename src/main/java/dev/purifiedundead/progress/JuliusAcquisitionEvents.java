package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.entity.BlightedKingEntity;
import dev.purifiedundead.entity.BlightedHealth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
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

/** Converts evokers, migrates legacy NBT kings, and preserves the existing Julius reward. */
public final class JuliusAcquisitionEvents {
    private static final String BLIGHTED_KING_KEY = "purified_undead:blighted_king";

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.juliusAcquisitionEnabled)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof Evoker evoker)
                || evoker instanceof BlightedKingEntity
                || evoker.getPersistentData().getBoolean(BLIGHTED_KING_KEY)
                || !event.getItemStack().is(ModItems.BLIGHTED_SPIRIT.get())
                || !(event.getLevel() instanceof ServerLevel level)
                || !ContractProgressService.hasContract(player)) {
            return;
        }

        BlightedKingEntity king = convert(evoker, level);
        if (king == null || !level.addFreshEntity(king)) return;
        evoker.discard();
        if (!player.getAbilities().instabuild) {
            event.getItemStack().shrink(1);
        }
        level.sendParticles(
                ParticleTypes.SOUL,
                king.getX(),
                king.getY() + 1.0D,
                king.getZ(),
                24,
                0.55D,
                0.8D,
                0.55D,
                0.035D);
        player.displayClientMessage(
                Component.translatable("message.purified_undead.julius.transformed"), false);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void migrateLegacyKing(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Evoker evoker)
                || evoker instanceof BlightedKingEntity
                || !evoker.isAlive()
                || !evoker.getPersistentData().getBoolean(BLIGHTED_KING_KEY)) return;
        // Run after chunk loading, never insert an entity from EntityJoinLevelEvent.
        // Keep the original alive if another mod rejects the replacement spawn.
        if (evoker.tickCount % 20 != 1) return;
        BlightedKingEntity king = convert(evoker, level);
        if (king != null && level.addFreshEntity(king)) evoker.discard();
    }

    private static BlightedKingEntity convert(Evoker original, ServerLevel level) {
        BlightedKingEntity king = ModEntities.BLIGHTED_KING.get().create(level);
        if (king == null) return null;
        float previousHealth = original.getHealth(), previousMaximum = original.getMaxHealth();
        CompoundTag data = original.saveWithoutId(new CompoundTag());
        data.remove("UUID");
        king.load(data);
        king.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BlightedKingEntity.MAX_HEALTH);
        king.setHealth(
                BlightedHealth.rescale(previousHealth, previousMaximum, king.getMaxHealth()));
        king.getPersistentData().putBoolean(BLIGHTED_KING_KEY, true);
        king.setPersistenceRequired();
        if (!king.hasCustomName()) {
            king.setCustomName(Component.translatable("entity.purified_undead.blighted_king"));
            king.setCustomNameVisible(true);
        }
        return king;
    }

    @SubscribeEvent
    public void onBlightedKingDeath(LivingDeathEvent event) {
        if (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.juliusAcquisitionEnabled)
                && event.getEntity() instanceof Evoker evoker
                && (evoker instanceof BlightedKingEntity
                        || evoker.getPersistentData().getBoolean(BLIGHTED_KING_KEY))
                && event.getSource().getEntity() instanceof ServerPlayer player
                && ContractProgressService.hasContract(player)) {
            WarriorRewardService.onJuliusDefeated(player);
        }
    }
}
