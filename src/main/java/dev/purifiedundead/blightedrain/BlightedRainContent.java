package dev.purifiedundead.blightedrain;

import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Objects;

/** Lifecycle boundary for the Blighted Rain update. */
public final class BlightedRainContent {
    private BlightedRainContent() {}

    public static void register(IEventBus modBus) {
        Objects.requireNonNull(modBus, "modBus");
        dev.purifiedundead.blightedrain.content.RainItems.register(modBus);
        dev.purifiedundead.blightedrain.mob.BlightedMobContent.register(modBus);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new BlightedRainEvents());
    }
}
