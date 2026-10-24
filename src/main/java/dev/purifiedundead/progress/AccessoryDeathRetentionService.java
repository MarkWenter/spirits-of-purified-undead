package dev.purifiedundead.progress;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.content.ModItemTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Moves death-dropped accessories through player data instead of the world drop list. */
final class AccessoryDeathRetentionService {
    private static final String DATA_KEY = PurifiedUndead.MOD_ID + ":retained_accessories";

    private AccessoryDeathRetentionService() {}

    static boolean isProtected(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItemTags.ACCESSORIES);
    }

    static int retain(ServerPlayer player, List<ItemStack> stacks) {
        var root = player.getPersistentData();
        CompoundTag retained = root.getCompound(DATA_KEY);
        var entries = retained.getList("stacks", Tag.TAG_COMPOUND);
        int count = 0;
        for (ItemStack stack : stacks) {
            if (entries.size() >= RetainedAccessoryBuffer.MAX_ENTRIES) break;
            try {
                CompoundTag serialized = stack.save(new CompoundTag());
                entries.add(serialized);
                retained.put("stacks", entries);
                root.put(DATA_KEY, retained);
                count++;
            } catch (RuntimeException | LinkageError failure) {
                dev.purifiedundead.util.FailureIsolation.report(
                        "accessory retention serialization", failure);
                break;
            }
        }
        return count;
    }

    static void copyAndRestore(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag root = original.getPersistentData();
        if (root.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            replacement.getPersistentData().put(DATA_KEY, root.getCompound(DATA_KEY).copy());
        }
        restore(replacement);
    }

    static void restore(ServerPlayer player) {
        // The old player continues ticking on the death screen; its inventory is not copied on
        // respawn.
        if (!player.isAlive()) return;
        var root = player.getPersistentData();
        if (!root.contains(DATA_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag retained = root.getCompound(DATA_KEY);
        var entries = retained.getList("stacks", Tag.TAG_COMPOUND);
        boolean changed = false;
        int examined = 0;
        for (int index = 0;
                index < entries.size() && examined++ < RetainedAccessoryBuffer.MAX_ENTRIES; ) {
            CompoundTag serialized = entries.getCompound(index);
            try {
                // Load/capability callbacks may mutate their input even when decoding fails.
                // Copy only this entry, never the whole retained buffer.
                ItemStack stack = ItemStack.of(serialized.copy());
                if (stack.isEmpty()) {
                    index++;
                    continue;
                }
                final int slot = index;
                final int beforeCount = stack.getCount();
                InventoryTransfer.insert(
                        player.getInventory(),
                        stack,
                        () -> {
                            if (stack.getCount() == beforeCount) return;
                            if (stack.isEmpty()) entries.remove(slot);
                            else entries.set(slot, stack.save(new CompoundTag()));
                            retained.put("stacks", entries);
                            if (entries.isEmpty()) root.remove(DATA_KEY);
                            else root.put(DATA_KEY, retained);
                        });
                changed |= stack.getCount() != beforeCount;
                if (!stack.isEmpty()) index++;
            } catch (RuntimeException | LinkageError failure) {
                dev.purifiedundead.util.FailureIsolation.report(
                        "retained accessory recovery; original entry retained", failure);
                index++;
            }
        }
        if (changed) RewardSounds.requestSync(player);
    }

    private static RetainedAccessoryBuffer load(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        return root.contains(DATA_KEY, Tag.TAG_COMPOUND)
                ? RetainedAccessoryBuffer.load(root.getCompound(DATA_KEY))
                : new RetainedAccessoryBuffer();
    }

    private static void save(ServerPlayer player, RetainedAccessoryBuffer buffer) {
        if (buffer.isEmpty()) {
            player.getPersistentData().remove(DATA_KEY);
        } else {
            player.getPersistentData().put(DATA_KEY, buffer.save());
        }
    }
}
