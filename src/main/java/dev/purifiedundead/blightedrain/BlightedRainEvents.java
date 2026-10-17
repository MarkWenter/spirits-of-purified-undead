package dev.purifiedundead.blightedrain;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Thin loader adapter. All mutations execute on the logical server's thread. */
public final class BlightedRainEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void rainItemDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        dev.purifiedundead.blightedrain.content.RainDrops.add(event);
    }

    @SubscribeEvent
    public void traitDamage(LivingHurtEvent e) {
        e.setAmount(
                dev.purifiedundead.blightedrain.mob.BlightedMobRules.attackDamage(
                        e.getSource(), e.getAmount()));
    }

    @SubscribeEvent
    public void traitAfterDamage(LivingDamageEvent e) {
        dev.purifiedundead.blightedrain.mob.BlightedMobRules.afterDamage(
                e.getEntity(), e.getSource(), e.getAmount());
    }

    @SubscribeEvent
    public void level(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START) return;
        var eventLevel = event.level;
        if (eventLevel instanceof ServerLevel level) RainController.tick(level);
    }

    @SubscribeEvent
    public void entity(LivingEvent.LivingTickEvent event) {
        RainEffects.tick(event.getEntity());
        dev.purifiedundead.blightedrain.content.RainEquipment.tick(event.getEntity());
    }

    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlightedRainData.get(player.server.overworld()).welcome(player);
            RainController.sync(player);
        }
    }

    @SubscribeEvent
    public void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RainController.sync(player);
    }

    @SubscribeEvent
    public void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RainController.sync(player);
    }

    @SubscribeEvent
    public void started(ServerStartedEvent event) {
        RainImmunity.load();
    }

    @SubscribeEvent
    public void stopped(ServerStoppedEvent event) {
        RainImmunity.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void immunity(LivingAttackEvent event) {
        if (event.getSource().is(RainEffects.DAMAGE)
                && (event.getEntity() instanceof net.minecraft.world.entity.player.Player
                        || RainEffects.damageImmune(event.getEntity()))) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void drops(LivingDropsEvent event) {
        if (event.getSource().is(RainEffects.DAMAGE)
                && !event.getEntity().getPersistentData().getBoolean(RainEffects.PLAYER_HIT))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void villagerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent e) {
        if (e.getSource().is(RainEffects.DAMAGE)
                && e.getEntity() instanceof net.minecraft.world.entity.Mob villager
                && (villager.getType() == net.minecraft.world.entity.EntityType.VILLAGER
                        || villager.getType() == net.minecraft.world.entity.EntityType.CHICKEN)
                && !RainEffects.damageImmune(villager)
                && villager.level() instanceof net.minecraft.server.level.ServerLevel level) {
            if (dev.purifiedundead.blightedrain.mob.BlightedMobConversions.convert(
                            villager, level, true)
                    != null) e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void extraFragment(net.minecraftforge.event.entity.living.LivingDropsEvent e) {
        if (e.getEntity() instanceof dev.purifiedundead.blightedrain.mob.BlightedVariant
                && e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level
                && level.getGameRules()
                        .getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))
            e.getDrops()
                    .add(
                            new net.minecraft.world.entity.item.ItemEntity(
                                    level,
                                    e.getEntity().getX(),
                                    e.getEntity().getY(),
                                    e.getEntity().getZ(),
                                    new net.minecraft.world.item.ItemStack(
                                            dev.purifiedundead.content.ModItems.BLIGHT_FRAGMENT
                                                    .get())));
    }

    @SubscribeEvent
    public void blightClick(
            net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract e) {
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player
                && e.getTarget() instanceof net.minecraft.world.entity.Mob mob
                && mob.level() instanceof net.minecraft.server.level.ServerLevel level
                && e.getItemStack().is(dev.purifiedundead.content.ModItems.BLIGHTED_SPIRIT.get())
                && dev.purifiedundead.progress.ContractProgressService.hasContract(player)
                && dev.purifiedundead.blightedrain.mob.BlightedMobConversions.clickable(mob)
                && !RainEffects.protectedMob(mob)) {
            if (dev.purifiedundead.progress.BlightedConversions.convert(mob, level) != null) {
                if (!player.getAbilities().instabuild) e.getItemStack().shrink(1);
                e.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                e.setCanceled(true);
            }
        }
    }
}
