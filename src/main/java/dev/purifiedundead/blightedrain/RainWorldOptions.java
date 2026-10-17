package dev.purifiedundead.blightedrain;

import net.minecraft.world.level.GameRules;

/** The vanilla creation screen serializes this option before the server is created. */
public final class RainWorldOptions {
    public static final GameRules.Key<GameRules.BooleanValue> ETERNAL_RAIN =
            GameRules.register(
                    "purifiedUndeadEternalRain",
                    GameRules.Category.UPDATES,
                    GameRules.BooleanValue.create(
                            true,
                            (server, value) -> {
                                if (server != null && server.overworld() != null)
                                    BlightedRainData.get(server.overworld())
                                            .setWeatherLocked(value.get());
                            }));

    public static void register() {}

    private RainWorldOptions() {}
}
