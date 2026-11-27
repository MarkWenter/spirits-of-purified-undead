package dev.purifiedundead.blightedarmaments;

import net.minecraftforge.eventbus.api.IEventBus;

/** Lifecycle boundary for the Blighted Armaments update. */
public final class BlightedArmamentsContent {
    private BlightedArmamentsContent() {}

    public static void register(IEventBus modBus) {
        BreathgoldContent.register(modBus);
    }
}
