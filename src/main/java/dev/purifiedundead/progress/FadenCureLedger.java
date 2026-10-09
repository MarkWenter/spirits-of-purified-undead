package dev.purifiedundead.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** World-owned fallback for cures that finish while their initiating player is offline. */
final class FadenCureLedger extends SavedData {
    private static final String DATA_NAME = "purified_undead_faden_cures";
    private final Set<UUID> pendingPlayers = new HashSet<>();

    static FadenCureLedger get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(FadenCureLedger::load, FadenCureLedger::new, DATA_NAME);
    }

    static FadenCureLedger load(CompoundTag tag) {
        FadenCureLedger ledger = new FadenCureLedger();
        ListTag list = tag.getList("pending", Tag.TAG_STRING);
        for (Tag entry : list) {
            try {
                ledger.pendingPlayers.add(UUID.fromString(entry.getAsString()));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed external data while keeping all valid pending owners.
            }
        }
        return ledger;
    }

    boolean add(UUID playerId) {
        boolean changed = pendingPlayers.add(playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    boolean take(UUID playerId) {
        boolean changed = pendingPlayers.remove(playerId);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        pendingPlayers.stream()
                .map(UUID::toString)
                .sorted()
                .forEach(value -> list.add(StringTag.valueOf(value)));
        tag.put("pending", list);
        return tag;
    }
}
