package dev.purifiedundead.progress;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

final class WarriorProgressStorage {
    private static final String DATA_KEY = PurifiedUndead.MOD_ID + ":warrior_progress";

    private WarriorProgressStorage() {
    }

    static WarriorProgress load(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        return root.contains(DATA_KEY, Tag.TAG_COMPOUND)
                ? WarriorProgress.load(root.getCompound(DATA_KEY)) : new WarriorProgress();
    }

    static void save(ServerPlayer player, WarriorProgress progress) {
        var before=ProgressManagement.snapshot(load(player));
        player.getPersistentData().put(DATA_KEY, progress.save());
        ModAdvancements.progression(player, progress);
        ProgressManagement.notifyChange(player,before,progress);
    }

    static void copy(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag root = original.getPersistentData();
        replacement.getPersistentData().putBoolean(ModAdvancements.MIGRATION, root.getBoolean(ModAdvancements.MIGRATION));
        if (root.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            replacement.getPersistentData().put(DATA_KEY, root.getCompound(DATA_KEY).copy());
        }
    }
}
