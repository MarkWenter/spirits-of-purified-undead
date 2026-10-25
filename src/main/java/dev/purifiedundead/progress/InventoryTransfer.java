package dev.purifiedundead.progress;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Restore vanilla inventory slots if a modified insertion or its pre-commit step fails. */
public final class InventoryTransfer {
    private InventoryTransfer() {}

    public static boolean insert(Inventory inventory, ItemStack stack, Runnable commit) {
        // Avoid capability/NBT copies when vanilla insertion cannot change any slot.
        // Creative insertion may consume a remainder even with a full inventory.
        if (!inventory.player.getAbilities().instabuild
                && inventory.getFreeSlot() < 0
                && (stack.isDamaged() || inventory.getSlotWithRemainingSpace(stack) < 0))
            return false;
        var before = new java.util.ArrayList<ItemStack>();
        for (int i = 0; i < inventory.getContainerSize(); i++)
            before.add(inventory.getItem(i).copy());
        try {
            boolean added = inventory.add(stack);
            commit.run();
            return added;
        } catch (RuntimeException | LinkageError failure) {
            // Do not continue if restoring the core inventory itself fails.
            try {
                for (int i = 0; i < before.size(); i++) inventory.setItem(i, before.get(i));
            } catch (RuntimeException | LinkageError rollbackFailure) {
                rollbackFailure.addSuppressed(failure);
                throw new AssertionError(
                        "Cannot safely restore inventory after failed insertion", rollbackFailure);
            }
            throw failure;
        }
    }
}
