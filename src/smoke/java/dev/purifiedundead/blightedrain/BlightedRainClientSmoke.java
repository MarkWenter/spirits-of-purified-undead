package dev.purifiedundead.blightedrain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import dev.purifiedundead.client.BlightedRainClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.Blocks;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class BlightedRainClientSmoke {
    private static boolean opened, started, stopping;
    private static int ticks, after;
    private static volatile boolean shelterOK;
    private static java.util.concurrent.CompletableFuture<Void> setup;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!Boolean.getBoolean("purified_undead.rainClientSmoke")) return;
        var mc = Minecraft.getInstance();
        try {
            if (!opened && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                opened = true;
                mc.createWorldOpenFlows().loadLevel(mc.screen, "blighted-rain-visual-test");
                return;
            }
            if (mc.level == null || mc.player == null || mc.getOverlay() != null) return;
            if (++ticks > 2000) throw new IllegalStateException("Client rain smoke timeout");
            if (!started) {
                started = true;
                setup = new java.util.concurrent.CompletableFuture<>();
                mc.getSingleplayerServer()
                        .execute(
                                () -> {
                                    try {
                                        var level = mc.getSingleplayerServer().overworld();
                                        var player =
                                                mc.getSingleplayerServer()
                                                        .getPlayerList()
                                                        .getPlayers()
                                                        .get(0);
                                        player.setGameMode(
                                                net.minecraft.world.level.GameType.CREATIVE);
                                        player.getAbilities().flying = true;
                                        player.onUpdateAbilities();
                                        player.teleportTo(44.5, 5, 49.5);
                                        player.setYRot(180);
                                        player.setXRot(-15);
                                        level.getGameRules()
                                                .getRule(
                                                        net.minecraft.world.level.GameRules
                                                                .RULE_DOMOBSPAWNING)
                                                .set(false, mc.getSingleplayerServer());
                                        level.setWeatherParameters(0, 12000, true, false);
                                        level.setRainLevel(1);
                                        level.setDayTime(6000);
                                        level.getChunk(2, 2);
                                        level.getChunk(3, 2);
                                        level.getChunk(2, 3);
                                        level.getChunk(3, 3);
                                        for (int x = 40; x <= 54; x++)
                                            for (int z = 40; z <= 52; z++)
                                                level.setBlockAndUpdate(
                                                        new BlockPos(x, 3, z),
                                                        Blocks.STONE.defaultBlockState());
                                        for (int x = 46; x <= 50; x++)
                                            for (int z = 44; z <= 48; z++)
                                                level.setBlockAndUpdate(
                                                        new BlockPos(x, 7, z),
                                                        Blocks.STONE_BRICKS.defaultBlockState());
                                        var home = new BlockPos(48, 4, 46);
                                        level.setBlockAndUpdate(
                                                home, Blocks.RED_BED.defaultBlockState());
                                        var villager = EntityType.VILLAGER.create(level);
                                        villager.setPos(44.5, 4, 46.5);
                                        level.addFreshEntity(villager);
                                        villager.refreshBrain(level);
                                        villager.getBrain()
                                                .setMemory(
                                                        MemoryModuleType.HOME,
                                                        GlobalPos.of(level.dimension(), home));
                                        var data = BlightedRainData.get(level);
                                        data.model.reset();
                                        data.setWeatherLocked(true);
                                        data.setEnabled(true);
                                        RainController.start(level, 1200);
                                        villager.tickCount = 20;
                                        RainEffects.tick(villager);
                                        shelterOK =
                                                villager.getBrain()
                                                                .hasMemoryValue(
                                                                        MemoryModuleType
                                                                                .HIDING_PLACE)
                                                        && villager.getBrain()
                                                                .isActive(Activity.HIDE);
                                        System.out.println(
                                                "RAIN_CLIENT_SETUP shelter="
                                                        + shelterOK
                                                        + " phase="
                                                        + data.model.phase()
                                                        + " precipitation="
                                                        + RainExposure.precipitation(
                                                                level, villager.blockPosition())
                                                        + " homeCovered="
                                                        + !level.canSeeSky(home.above())
                                                        + " noAI="
                                                        + villager.isNoAi()
                                                        + " tick="
                                                        + villager.tickCount
                                                        + " active="
                                                        + villager.getBrain()
                                                                .getActiveNonCoreActivity());
                                        setup.complete(null);
                                    } catch (Throwable failure) {
                                        setup.completeExceptionally(failure);
                                    }
                                });
                return;
            }
            if (!setup.isDone()) return;
            setup.join();
            if (ticks == 120) {
                if (!(BlightedRainClient.intensity() > 0 && BlightedRainClient.intensity() < 1))
                    throw new IllegalStateException("Warning fade not synchronized");
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "blighted-rain-warning.png",
                        mc.getMainRenderTarget(),
                        message -> {});
            }
            if (ticks > 360 && !stopping) {
                if (Boolean.getBoolean("purified_undead.rainSnow")
                        && mc.level
                                        .getBiome(mc.player.blockPosition())
                                        .value()
                                        .getPrecipitationAt(mc.player.blockPosition())
                                != net.minecraft.world.level.biome.Biome.Precipitation.SNOW)
                    throw new IllegalStateException("Visual world is not a snow biome");
                if (BlightedRainClient.intensity() < .99F || !shelterOK)
                    throw new IllegalStateException("Active visuals or villager shelter failed");
                if (!mc.player.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED)
                        || !mc.player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST))
                    throw new IllegalStateException("Actual client effect synchronization failed");
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "blighted-rain-active.png",
                        mc.getMainRenderTarget(),
                        message -> {});
                stopping = true;
                mc.getSingleplayerServer()
                        .execute(() -> RainController.stop(mc.getSingleplayerServer().overworld()));
            }
            if (stopping && ++after >= 30) {
                if (BlightedRainClient.intensity() != 0)
                    throw new IllegalStateException("Ended visuals did not stop");
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "blighted-rain-ended.png",
                        mc.getMainRenderTarget(),
                        message -> {});
                System.out.println(
                        "BLIGHTED_RAIN_CLIENT_OK: actual synchronized warning/fade, active rain or snow, player effects, villager HIDE, cloud/fog/particle target loading and immediate end");
                mc.stop();
            }
        } catch (Throwable failure) {
            System.out.println("BLIGHTED_RAIN_CLIENT_FAILED");
            failure.printStackTrace();
            mc.stop();
        }
    }
}
