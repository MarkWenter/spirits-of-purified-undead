package dev.purifiedundead.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/** Non-cancellable server-thread notification after progression has been saved.
 * Listen on the Forge game event bus. Do not modify progression synchronously inside a listener.
 */
public final class ProgressChangedEvent extends Event {
    private final ServerPlayer player;
    private final ProgressSnapshot before, after;

    public ProgressChangedEvent(
            ServerPlayer player, ProgressSnapshot before, ProgressSnapshot after) {
        this.player = player;
        this.before = before;
        this.after = after;
    }

    public ServerPlayer player() {
        return player;
    }

    public ProgressSnapshot before() {
        return before;
    }

    public ProgressSnapshot after() {
        return after;
    }
}
