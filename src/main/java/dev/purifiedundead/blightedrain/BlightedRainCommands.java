package dev.purifiedundead.blightedrain;

import static net.minecraft.commands.Commands.*;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Administrator test controls; start never silently changes clear weather or bypasses cooldown. */
public final class BlightedRainCommands {
    private BlightedRainCommands() {}

    private static SimpleCommandExceptionType error(String key) {
        return new SimpleCommandExceptionType(
                Component.translatable("command.purified_undead.rain." + key));
    }

    private static ServerLevel level(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (source.getLevel().dimension() != Level.OVERWORLD)
            throw error("wrong_dimension").create();
        return source.getLevel();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> tree() {
        return literal("rain")
                .requires(s -> s.hasPermission(2))
                .then(
                        literal("status")
                                .executes(
                                        c -> {
                                            var l = level(c.getSource());
                                            var d = BlightedRainData.get(l);
                                            reply(
                                                    c.getSource(),
                                                    "status",
                                                    d.model.phase().name(),
                                                    d.model.remaining(l.getGameTime()) / 20,
                                                    d.weatherLocked());
                                            return 1;
                                        }))
                .then(
                        literal("start")
                                .executes(c -> start(c.getSource(), 0))
                                .then(
                                        argument("seconds", IntegerArgumentType.integer(1, 600))
                                                .executes(
                                                        c ->
                                                                start(
                                                                        c.getSource(),
                                                                        IntegerArgumentType
                                                                                .getInteger(
                                                                                        c,
                                                                                        "seconds")))))
                .then(
                        literal("stop")
                                .executes(
                                        c -> {
                                            var l = level(c.getSource());
                                            if (!BlightedRainData.get(l).model.occupied())
                                                throw error("not_active").create();
                                            RainController.stop(l);
                                            reply(c.getSource(), "stopped");
                                            return 1;
                                        }))
                .then(
                        literal("reset")
                                .executes(
                                        c -> {
                                            var l = level(c.getSource());
                                            RainController.stop(l);
                                            var d = BlightedRainData.get(l);
                                            d.model.reset();
                                            d.setDirty();
                                            RainController.syncAll(l);
                                            reply(c.getSource(), "reset");
                                            return 1;
                                        }))
                .then(
                        literal("weather")
                                .then(literal("locked").executes(c -> weather(c.getSource(), true)))
                                .then(
                                        literal("vanilla")
                                                .executes(c -> weather(c.getSource(), false))));
    }

    private static int start(CommandSourceStack source, int seconds)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var l = level(source);
        var d = BlightedRainData.get(l);
        if (!l.getLevelData().isRaining()) throw error("clear_weather").create();
        if (!d.enabled()) throw error("disabled").create();
        if (d.suppressed()) throw error("beacon_active").create();
        if (d.model.occupied()) throw error("already_active").create();
        if (d.model.phase() == BlightedRainModel.Phase.COOLDOWN) throw error("cooldown").create();
        int duration =
                seconds == 0
                        ? BlightedRainModel.MIN_DURATION
                                + l.random.nextInt(
                                        BlightedRainModel.MAX_DURATION
                                                - BlightedRainModel.MIN_DURATION
                                                + 1)
                        : seconds * 20;
        RainController.start(l, duration);
        reply(source, "started", duration / 20);
        return 1;
    }

    private static int weather(CommandSourceStack source, boolean locked)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var l = level(source);
        l.getGameRules().getRule(RainWorldOptions.ETERNAL_RAIN).set(locked, source.getServer());
        reply(source, "weather", locked);
        return 1;
    }

    private static void reply(CommandSourceStack source, String key, Object... args) {
        source.sendSuccess(
                () -> Component.translatable("command.purified_undead.rain." + key, args), false);
    }
}
