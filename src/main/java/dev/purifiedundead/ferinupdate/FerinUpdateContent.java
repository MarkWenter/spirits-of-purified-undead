package dev.purifiedundead.ferinupdate;

import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Objects;

/** Lifecycle boundary for the Ferin update; concrete content is intentionally deferred. */
public final class FerinUpdateContent {
    private FerinUpdateContent() {}

    public static void register(IEventBus modBus) {
        Objects.requireNonNull(modBus, "modBus");
        // Install update-specific registrations here once their behavior is defined.
        // Existing Ferin combat and rendering retain their current owners.
    }
}
