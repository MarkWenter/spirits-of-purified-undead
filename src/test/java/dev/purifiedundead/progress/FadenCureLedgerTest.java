package dev.purifiedundead.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FadenCureLedgerTest {
    @Test
    void offlineCureOwnerSurvivesSaveAndIsConsumedOnce() {
        UUID owner = UUID.fromString("64caf58d-27c4-4011-84a5-594e23d6eade");
        FadenCureLedger ledger = new FadenCureLedger();
        assertTrue(ledger.add(owner));
        assertFalse(ledger.add(owner));

        FadenCureLedger restored = FadenCureLedger.load(ledger.save(new CompoundTag()));
        assertTrue(restored.take(owner));
        assertFalse(restored.take(owner));
    }

    @Test
    void malformedExternalUuidDoesNotDiscardValidEntries() {
        UUID owner = UUID.fromString("4096026f-78ef-4d37-8de0-3d5c2aca78ce");
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        list.add(StringTag.valueOf("not-a-uuid"));
        list.add(StringTag.valueOf(owner.toString()));
        tag.put("pending", list);

        assertTrue(FadenCureLedger.load(tag).take(owner));
    }
}
