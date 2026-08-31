package com.apexballistics.block;

import com.apexballistics.blockentity.CruiseLauncherBlockEntity;
import com.apexballistics.item.TargetDesignatorItem;
import com.apexballistics.registry.ModDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CruiseLauncherBlock extends BaseEntityBlock {
    public static final MapCodec<CruiseLauncherBlock> CODEC = simpleCodec(CruiseLauncherBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public CruiseLauncherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT)
                .setValue(TRIGGERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static BlockPos getBasePos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == BedPart.FOOT ? pos : pos.relative(state.getValue(FACING).getOpposite());
    }

    private static Direction neighbourDirection(BedPart part, Direction facing) {
        return part == BedPart.FOOT ? facing : facing.getOpposite();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        BlockPos headPos = pos.relative(facing);
        Level level = context.getLevel();
        if (!level.getBlockState(headPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(headPos)) {
            return null;
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, BedPart.FOOT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos headPos = pos.relative(state.getValue(FACING));
            level.setBlock(headPos, state.setValue(PART, BedPart.HEAD), Block.UPDATE_ALL);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == neighbourDirection(state.getValue(PART), state.getValue(FACING))) {
            return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART)
                    ? state
                    : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BedPart part = state.getValue(PART);
            if (part == BedPart.FOOT) {
                BlockPos other = pos.relative(neighbourDirection(part, state.getValue(FACING)));
                BlockState otherState = level.getBlockState(other);
                if (otherState.is(this) && otherState.getValue(PART) == BedPart.HEAD) {
                    level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, other, Block.getId(otherState));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return this.rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, TRIGGERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == BedPart.FOOT ? new CruiseLauncherBlockEntity(pos, state) : null;
    }

    @Nullable
    private CruiseLauncherBlockEntity getBaseEntity(Level level, BlockPos pos, BlockState state) {
        BlockEntity entity = level.getBlockEntity(getBasePos(state, pos));
        return entity instanceof CruiseLauncherBlockEntity launcher ? launcher : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        CruiseLauncherBlockEntity launcher = this.getBaseEntity(level, pos, state);
        if (launcher == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof TargetDesignatorItem) {
            BlockPos target = stack.get(ModDataComponents.TARGET_POS.get());
            if (target != null && !level.isClientSide) {
                launcher.setTarget(target);
            }
        }
        this.openGui(level, pos, state, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        this.openGui(level, pos, state, player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void openGui(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CruiseLauncherBlockEntity launcher = this.getBaseEntity(level, pos, state);
        if (launcher != null) {
            serverPlayer.openMenu(launcher, launcher.getBlockPos());
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        BlockPos basePos = getBasePos(state, pos);
        BlockState baseState = level.getBlockState(basePos);
        if (!baseState.is(this) || baseState.getValue(PART) != BedPart.FOOT) {
            return;
        }
        boolean powered = level.hasNeighborSignal(basePos) || level.hasNeighborSignal(basePos.relative(baseState.getValue(FACING)));
        boolean triggered = baseState.getValue(TRIGGERED);
        if (powered && !triggered) {
            level.setBlock(basePos, baseState.setValue(TRIGGERED, true), Block.UPDATE_CLIENTS);
            CruiseLauncherBlockEntity launcher = this.getBaseEntity(level, basePos, baseState);
            if (launcher != null) {
                launcher.tryLaunch(null);
            }
        } else if (!powered && triggered) {
            level.setBlock(basePos, baseState.setValue(TRIGGERED, false), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && state.getValue(PART) == BedPart.FOOT
                && level.getBlockEntity(pos) instanceof CruiseLauncherBlockEntity launcher) {
            launcher.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
