package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Awards Ulv only after a real, completed night sleep with the two ritual items in the specified hands. */
public final class UlvAcquisitionEvents {
    private final Set<UUID> startedAtNight = new HashSet<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvAcquisitionEnabled)
                || event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.isSleeping() && !player.level().isDay() && hasRitualItems(player)) {
            startedAtNight.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onWake(PlayerWakeUpEvent event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvAcquisitionEnabled)
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean beganCorrectly = startedAtNight.remove(player.getUUID());
        if (!beganCorrectly
                || event.wakeImmediately()
                || player.getSleepTimer()
                        < PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvMinimumSleepTicks)
                || !hasRitualItems(player)
                || !WarriorRewardService.grantUlv(player)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
            player.getOffhandItem().shrink(1);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        startedAtNight.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        startedAtNight.clear();
    }

    private static boolean hasRitualItems(ServerPlayer player) {
        return player.getMainHandItem().is(ModItems.SNOW_FLOWER.get())
                && player.getOffhandItem().is(Items.GHAST_TEAR);
    }
}
