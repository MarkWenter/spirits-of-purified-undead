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

    public PurificationFoundryMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(4), new SimpleContainerData(3));
    }

    public PurificationFoundryMenu(int id, Inventory inv, Container container, ContainerData data) {
        super(FoundryContent.MENU.get(), id);
        checkContainerSize(container, 4);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        container.startOpen(inv.player);
        addSlot(
                new Slot(container, 0, 92, 23) {
                    @Override
                    public boolean mayPlace(ItemStack s) {
                        return !s.is(dev.purifiedundead.content.ModItems.BLIGHTED_SPIRIT.get())
                                && !s.is(dev.purifiedundead.content.ModItems.PURE_CRYSTAL.get());
                    }
                });
        addSlot(
                new Slot(container, 1, 56, 65) {
                    @Override
                    public boolean mayPlace(ItemStack s) {
                        return s.is(dev.purifiedundead.content.ModItems.BLIGHTED_SPIRIT.get());
                    }
                });
        addSlot(
                new Slot(container, 2, 136, 65) {
                    @Override
                    public boolean mayPlace(ItemStack s) {
                        return s.is(dev.purifiedundead.content.ModItems.PURE_CRYSTAL.get());
                    }
                });
        addSlot(
                new Slot(container, 3, 92, 113) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return data.get(2) == 0
                                && !stack.is(
                                        dev.purifiedundead.content.ModItems.BLIGHTED_SPIRIT.get())
                                && !stack.is(
                                        dev.purifiedundead.content.ModItems.PURE_CRYSTAL.get());
                    }
                });
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 20 + col * 18, 150 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 20 + col * 18, 208));
        addDataSlots(data);
    }

    public int progressPixels(int height) {
        return data.get(1) <= 0
                ? 0
                : Math.min(height, Math.max(0, data.get(0)) * height / data.get(1));
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, 40, true)) return ItemStack.EMPTY;
        } else {
            if (stack.is(dev.purifiedundead.content.ModItems.BLIGHTED_SPIRIT.get())) {
                if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else if (stack.is(dev.purifiedundead.content.ModItems.PURE_CRYSTAL.get())) {
                if (!moveItemStackTo(stack, 2, 3, false)) return ItemStack.EMPTY;
            } else if (!container.getItem(0).isEmpty()
                    && data.get(2) == 0
                    && FoundryRecipes.bottom(stack)) {
                if (!moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
