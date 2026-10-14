package dev.purifiedundead.blightedrain;

import java.util.Objects;
import net.minecraftforge.eventbus.api.IEventBus;

/** Lifecycle boundary for the Blighted Rain update. */
public final class BlightedRainContent {
    private BlightedRainContent() {}

    public static void register(IEventBus modBus) {
        Objects.requireNonNull(modBus, "modBus");
        dev.purifiedundead.blightedrain.mob.BlightedMobContent.register(modBus);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new BlightedRainEvents());
    }
}
