package dev.purifiedundead.blightedrain;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import dev.purifiedundead.network.BlightedRainPacket;

/** Coordinates weather policy, world-owned state and edge-triggered client notifications. */
public final class RainController {
    private RainController() {}

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) return;
        var data = BlightedRainData.get(level);
        long now = level.getGameTime();
        if (data.lastProcessedTime == now) return;
        data.lastProcessedTime = now;
        if (data.weatherLocked()) ensureRain(level);
        var before = data.model.phase();
        if ((!data.enabled() || !level.getLevelData().isRaining()) && data.model.occupied())
            data.model.stop(now);
        else data.model.advance(now);
        if (before != data.model.phase()) {
            data.setDirty();
            if (data.model.phase() == BlightedRainModel.Phase.COOLDOWN) announce(level, "ended");
            syncAll(level);
        }
        if (data.enabled()
                && data.model.canRoll(
                        level.getLevelData().isRaining(), !level.players().isEmpty(), now)
                && level.random.nextDouble() < BlightedRainModel.CHANCE_PER_SECOND)
            start(
                    level,
                    BlightedRainModel.MIN_DURATION
                            + level.random.nextInt(
                                    BlightedRainModel.MAX_DURATION
                                            - BlightedRainModel.MIN_DURATION
                                            + 1));
        if (data.model.phase() == BlightedRainModel.Phase.WARNING && now % 20 == 0) syncAll(level);
    }

    private static void ensureRain(ServerLevel level) {
        var weather = (ServerLevelData) level.getLevelData();
        boolean repair = !weather.isRaining() || level.getRainLevel(1) < .2F;
        if (weather.getClearWeatherTime() > 0) weather.setClearWeatherTime(0);
        if (weather.getRainTime() <= 1) weather.setRainTime(12000);
        if (!weather.isRaining()) weather.setRaining(true);
        if (repair) {
            level.setRainLevel(1);
            for (var player : level.players()) {
                player.connection.send(
                        new net.minecraft.network.protocol.game.ClientboundGameEventPacket(
                                net.minecraft.network.protocol.game.ClientboundGameEventPacket
                                        .START_RAINING,
                                0));
                player.connection.send(
                        new net.minecraft.network.protocol.game.ClientboundGameEventPacket(
                                net.minecraft.network.protocol.game.ClientboundGameEventPacket
                                        .RAIN_LEVEL_CHANGE,
                                1));
            }
        }
    }

    public static void start(ServerLevel level, int duration) {
        if (level.dimension() != Level.OVERWORLD || !level.getLevelData().isRaining())
            throw new IllegalStateException("Rain required in the overworld");
        var data = BlightedRainData.get(level);
        if (!data.enabled()) throw new IllegalStateException("Rain events disabled");
        data.model.start(level.getGameTime(), duration);
        data.setDirty();
        announce(level, "warning");
        syncAll(level);
    }

    public static void stop(ServerLevel level) {
        var data = BlightedRainData.get(level);
        if (!data.model.occupied()) return;
        data.model.stop(level.getGameTime());
        data.setDirty();
        announce(level, "ended");
        syncAll(level);
    }

    /** Future ritual/configuration seam; disabling events immediately stops the current one. */
    public static void setEnabled(ServerLevel level, boolean enabled) {
        var data = BlightedRainData.get(level);
        data.setEnabled(enabled);
        if (!enabled) stop(level);
        syncAll(level);
    }

    public static void syncAll(ServerLevel level) {
        for (var player : level.players()) sync(player);
    }

    public static void sync(ServerPlayer player) {
        if (player.connection == null) return;
        boolean overworld = player.level().dimension() == Level.OVERWORLD;
        var data = BlightedRainData.get(player.server.overworld());
        var packet =
                new BlightedRainPacket(
                        overworld ? data.model.phase().ordinal() : 0,
                        overworld
                                ? data.model.remaining(player.server.overworld().getGameTime())
                                : 0);
        dev.purifiedundead.network.ModNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static void announce(ServerLevel level, String key) {
        for (var player : level.players()) {
            player.sendSystemMessage(Component.translatable("message.purified_undead.rain." + key));
            if (key.equals("warning"))
                player.playNotifySound(
                        dev.purifiedundead.content.ModSounds.find("blighted_rain_warning"),
                        net.minecraft.sounds.SoundSource.MASTER,
                        1,
                        1);
        }
    }
}
