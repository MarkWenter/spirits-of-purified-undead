package dev.purifiedundead.purification;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.*;

/** Existing consumable is also a plant; the block has no second item identity. */
public final class LotusBlock extends BushBlock {
    private final boolean scarlet;

    public LotusBlock(boolean scarlet) {
        super(Properties.of().noCollission().instabreak().sound(SoundType.GRASS));
        this.scarlet = scarlet;
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter level, BlockPos pos) {
        return floor.is(Blocks.GRASS_BLOCK)
                || floor.is(net.minecraft.tags.BlockTags.DIRT)
                || floor.is(Blocks.FARMLAND);
    }

    @Override
    public VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(2, 0, 2, 14, 13, 14);
    }

    @Override
    public net.minecraft.world.item.Item asItem() {
        return scarlet
                ? dev.purifiedundead.content.ModItems.SCARLET_LOTUS.get()
                : dev.purifiedundead.content.ModItems.WHITE_LOTUS.get();
    }
}
