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
        RetainedAccessoryBuffer buffer = load(player);
        int retained = 0;
        for (ItemStack stack : stacks) {
            CompoundTag serialized = stack.save(new CompoundTag());
            if (!buffer.add(serialized)) {
                break;
            }
            retained++;
        }
        save(player, buffer);
        return retained;
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
        RetainedAccessoryBuffer source = load(player);
        if (source.isEmpty()) {
            return;
        }
        RetainedAccessoryBuffer remaining = new RetainedAccessoryBuffer();
        for (CompoundTag serialized : source.entries()) {
            ItemStack stack = ItemStack.of(serialized);
            if (stack.isEmpty()) {
                continue;
            }
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                remaining.add(stack.save(new CompoundTag()));
            }
        }
        save(player, remaining);
        player.inventoryMenu.broadcastChanges();
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
