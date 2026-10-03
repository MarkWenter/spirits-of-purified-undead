package dev.purifiedundead.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Four persistent slots. Processing and ingredient restrictions await the slate update's next part. */
public final class PurificationFoundryEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int INPUT = 0, LEFT_FUEL = 1, RIGHT_FUEL = 2, OUTPUT = 3;
    private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int progress, duration;
    private java.util.EnumMap<Direction, net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>> handlers = createHandlers();
    private java.util.EnumMap<Direction, net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>> createHandlers() {
        var result = new java.util.EnumMap<Direction, net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>>(Direction.class);
        for (Direction face : Direction.values()) result.put(face, net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.SidedInvWrapper(this, face)));
        return result;
    }
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, Direction face) {
        if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER)
            return face == null || isRemoved() ? net.minecraftforge.common.util.LazyOptional.empty() : handlers.get(face).cast();
        return super.getCapability(capability, face);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); handlers.values().forEach(net.minecraftforge.common.util.LazyOptional::invalidate); }
    @Override public void reviveCaps() { super.reviveCaps(); handlers = createHandlers(); }
    private final ContainerData data = new ContainerData() {
        public int get(int i) { return i == 0 ? progress : duration; }
        public void set(int i, int v) { if (i == 0) progress = v; else duration = v; }
        public int getCount() { return 2; }
    };
    public PurificationFoundryEntity(BlockPos p, BlockState s) { super(FoundryContent.ENTITY.get(), p, s); }
    @Override protected Component getDefaultName() { return Component.translatable("block.purified_undead.purification_foundry"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inv) { return new PurificationFoundryMenu(id, inv, this, data); }
    @Override public int getContainerSize() { return 4; }
    @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5) <= 64; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { var result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged(); }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot != OUTPUT; }
    @Override public int[] getSlotsForFace(Direction side) {
        Direction front = getBlockState().getValue(PurificationFoundryBlock.FACING);
        if (side == Direction.UP) return new int[]{INPUT};
        if (side == Direction.DOWN) return new int[]{OUTPUT};
        // Left/right are viewed by a player looking at the front face.
        if (side == front.getClockWise()) return new int[]{LEFT_FUEL};
        if (side == front.getCounterClockWise()) return new int[]{RIGHT_FUEL};
        return new int[0];
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        int[] exposed = getSlotsForFace(side);
        return slot != OUTPUT && exposed.length == 1 && exposed[0] == slot;
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return side == Direction.DOWN && slot == OUTPUT; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); ContainerHelper.saveAllItems(tag, items); }
    @Override public void load(CompoundTag tag) { super.load(tag); items = NonNullList.withSize(4, ItemStack.EMPTY); ContainerHelper.loadAllItems(tag, items); }

    /** Server-owned presentation hook; no recipe, fuel consumption or automatic production is defined yet. */
    public void setWorkProgress(int completed, int total) {
        if (level == null || level.isClientSide) return;
        duration = Math.max(0, Math.min(32767, total));
        progress = Math.max(0, Math.min(duration, completed));
        boolean lit = duration > 0;
        if (getBlockState().getValue(PurificationFoundryBlock.LIT) != lit)
            level.setBlock(worldPosition, getBlockState().setValue(PurificationFoundryBlock.LIT, lit), 3);
        setChanged();
    }
}
