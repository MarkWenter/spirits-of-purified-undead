package dev.purifiedundead.foundry;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class PurificationFoundryMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    public PurificationFoundryMenu(int id, Inventory inv) { this(id, inv, new SimpleContainer(4), new SimpleContainerData(2)); }
    public PurificationFoundryMenu(int id, Inventory inv, Container container, ContainerData data) {
        super(FoundryContent.MENU.get(), id);
        checkContainerSize(container, 4); checkContainerDataCount(data, 2);
        this.container = container; this.data = data; container.startOpen(inv.player);
        addSlot(new Slot(container, 0, 92, 23));
        addSlot(new Slot(container, 1, 56, 65));
        addSlot(new Slot(container, 2, 136, 65));
        addSlot(new Slot(container, 3, 92, 113) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col + row * 9 + 9, 20 + col * 18, 150 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 20 + col * 18, 208));
        addDataSlots(data);
    }
    public int progressPixels(int height) { return data.get(1) <= 0 ? 0 : Math.min(height, Math.max(0, data.get(0)) * height / data.get(1)); }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, 40, true)) return ItemStack.EMPTY;
        } else {
            // Ingredients/fuels are not classified yet; shift-click has one predictable destination.
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
