package dev.purifiedundead.combat;

import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

final class FerinPlayerStateStorage {
    static final String KEY = "purified_undead:ferin_state";

    private FerinPlayerStateStorage() {}

    static boolean has(Player player) {
        return player.getPersistentData().contains(KEY, Tag.TAG_COMPOUND);
    }

    static FerinPlayerState load(Player player) {
        return FerinPlayerState.load(player.getPersistentData().getCompound(KEY));
    }

    static void save(Player player, FerinPlayerState state) {
        player.getPersistentData().put(KEY, state.save());
    }

    static void copy(Player original, Player replacement) {
        if (has(original)) {
            replacement
                    .getPersistentData()
                    .put(KEY, original.getPersistentData().getCompound(KEY).copy());
        }
    }
}
