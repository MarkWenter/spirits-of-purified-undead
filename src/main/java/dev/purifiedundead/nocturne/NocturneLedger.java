package dev.purifiedundead.nocturne;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

/** World-owned bindings also receive maid events while the owner is offline. */
final class NocturneLedger extends SavedData {
    private static final String DATA_NAME = "purified_undead_knight_nocturne";
    private CompoundTag records = new CompoundTag();

    static NocturneLedger get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(NocturneLedger::load, NocturneLedger::new, DATA_NAME);
    }

    static NocturneLedger load(CompoundTag tag) {
        NocturneLedger ledger = new NocturneLedger();
        ledger.records = tag.getCompound("records");
        return ledger;
    }

    CompoundTag record(UUID id) { return records.getCompound(id.toString()); }
    void bind(UUID owner, UUID charmId, UUID epoch) {
        // Item data identifies a charm. Snapshots, death and credit belong exclusively to this ledger.
        CompoundTag binding = new CompoundTag();
        binding.putUUID("owner", owner);
        binding.putUUID("id", charmId);
        binding.putUUID("epoch", epoch);
        records.put(owner.toString(), binding);
        setDirty();
    }
    boolean release(UUID id) {
        if (!records.contains(id.toString())) return false;
        records.remove(id.toString());
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("records", records.copy());
        return tag;
    }
}
