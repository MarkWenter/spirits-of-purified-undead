package dev.purifiedundead.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class PurificationFoundryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public PurificationFoundryBlock() {
        super(Properties.of().strength(3.5F).requiresCorrectToolForDrops().sound(SoundType.STONE));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, LIT); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }
    @Override public BlockState mirror(BlockState s, Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new PurificationFoundryEntity(p, s); }
    @Override public InteractionResult use(BlockState s, Level level, BlockPos p, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(p) instanceof PurificationFoundryEntity entity) player.openMenu(entity);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void onRemove(BlockState s, Level level, BlockPos p, BlockState next, boolean moving) {
        if (!s.is(next.getBlock())) {
            if (level.getBlockEntity(p) instanceof PurificationFoundryEntity entity) {
                Containers.dropContents(level, p, entity);
                level.updateNeighbourForOutputSignal(p, this);
            }
            super.onRemove(s, level, p, next, moving);
        }
    }
}
