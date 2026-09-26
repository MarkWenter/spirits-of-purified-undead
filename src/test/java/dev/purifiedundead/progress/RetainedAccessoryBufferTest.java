package dev.purifiedundead.progress;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetainedAccessoryBufferTest {
    @Test
    void serializedEntriesSurviveRoundTripWithoutSharingReferences() {
        RetainedAccessoryBuffer buffer = new RetainedAccessoryBuffer();
        CompoundTag stack = new CompoundTag();
        stack.putString("id", "purified_undead:ferin_warrior");
        assertTrue(buffer.add(stack));
        stack.putString("id", "minecraft:air");

        RetainedAccessoryBuffer restored = RetainedAccessoryBuffer.load(buffer.save());
        assertEquals(1, restored.entries().size());
        assertEquals("purified_undead:ferin_warrior", restored.entries().get(0).getString("id"));
    }

    @Test
    void malformedAndExcessEntriesAreBounded() {
        RetainedAccessoryBuffer buffer = new RetainedAccessoryBuffer();
        assertFalse(buffer.add(new CompoundTag()));
        for (int index = 0; index < RetainedAccessoryBuffer.MAX_ENTRIES; index++) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("index", index);
            assertTrue(buffer.add(entry));
        }
        CompoundTag overflow = new CompoundTag();
        overflow.putInt("index", 999);
        assertFalse(buffer.add(overflow));
        assertEquals(RetainedAccessoryBuffer.MAX_ENTRIES, buffer.entries().size());
    }

    @Test
    void futureVersionKeepsReadablePendingStacks() {
        RetainedAccessoryBuffer buffer = new RetainedAccessoryBuffer();
        CompoundTag entry = new CompoundTag();
        entry.putString("id", "purified_undead:ancient_contract");
        buffer.add(entry);
        CompoundTag future = buffer.save();
        future.putInt("version", 99);

        assertEquals(1, RetainedAccessoryBuffer.load(future).entries().size());
    }
}
