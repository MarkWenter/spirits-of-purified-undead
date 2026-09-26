package dev.purifiedundead.progress;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/** Bounded serialized buffer used between confirmed player drops and respawn cloning. */
public final class RetainedAccessoryBuffer {
    private static final int FORMAT_VERSION = 1;
    static final int MAX_ENTRIES = 128;

    private final List<CompoundTag> entries = new ArrayList<>();

    public boolean add(CompoundTag serializedStack) {
        if (serializedStack == null || serializedStack.isEmpty() || entries.size() >= MAX_ENTRIES) {
            return false;
        }
        entries.add(serializedStack.copy());
        return true;
    }

    public List<CompoundTag> entries() {
        return entries.stream().map(CompoundTag::copy).toList();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        root.putInt("version", FORMAT_VERSION);
        ListTag stacks = new ListTag();
        entries.forEach(stacks::add);
        root.put("stacks", stacks);
        return root;
    }

    public static RetainedAccessoryBuffer load(CompoundTag root) {
        RetainedAccessoryBuffer buffer = new RetainedAccessoryBuffer();
        // The stack payload is self-describing. Preserve readable entries from a future
        // version rather than deleting death-retained items after a downgrade.
        ListTag stacks = root.getList("stacks", Tag.TAG_COMPOUND);
        for (int index = 0; index < stacks.size() && index < MAX_ENTRIES; index++) {
            buffer.add(stacks.getCompound(index));
        }
        return buffer;
    }
}
