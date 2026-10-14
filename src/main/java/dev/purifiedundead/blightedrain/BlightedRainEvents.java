package dev.purifiedundead.blightedrain;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** Thin loader adapter. All mutations execute on the logical server's thread. */
public final class BlightedRainEvents {
    @SubscribeEvent
    public void level(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START) return;
        var eventLevel = event.level;
        if (eventLevel instanceof ServerLevel level) RainController.tick(level);
    }

    @SubscribeEvent
    public void entity(LivingEvent.LivingTickEvent event) {
        RainEffects.tick(event.getEntity());
    }

    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) RainController.sync(player);
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
                        || RainEffects.protectedMob(event.getEntity()))) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void drops(LivingDropsEvent event) {
        if (event.getSource().is(RainEffects.DAMAGE)
                && !event.getEntity().getPersistentData().getBoolean(RainEffects.PLAYER_HIT))
            event.setCanceled(true);
    }
}
