package dev.purifiedundead.blightedrain.content;

import dev.purifiedundead.blightedrain.BlightedRainData;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** World-saved activation survives chunk unload without chunk tickets. */
public final class StillRainBeaconBlock extends Block {
    public StillRainBeaconBlock() {
        super(Properties.of().strength(3).noOcclusion().lightLevel(s -> 12));
    }

    public static boolean isActive(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof StillRainBeaconBlock
                && level.getBlockState(pos.below()).is(Blocks.AMETHYST_BLOCK)
                && level.hasNeighborSignal(pos);
    }

    private void update(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server && server.dimension() == Level.OVERWORLD) {
            BlightedRainData.get(server).setBeacon(pos, isActive(server, pos));
            server.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void onPlace(
            BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        update(level, pos);
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block other,
            BlockPos from,
            boolean moving) {
        update(level, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        update(level, pos);
    }

    @Override
    public void onRemove(
            BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!next.is(this)
                && level instanceof ServerLevel server
                && server.dimension() == Level.OVERWORLD)
            BlightedRainData.get(server).setBeacon(pos, false);
        super.onRemove(state, level, pos, next, moving);
    }
}
