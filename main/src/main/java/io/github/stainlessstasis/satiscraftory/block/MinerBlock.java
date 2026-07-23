package io.github.stainlessstasis.satiscraftory.block;

import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockDemolition;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPlacement;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPreviewer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block_entity.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MinerBlock extends ProducerBlock implements MultiblockPreviewer<MinerBlock> {
    public static final int NODE_SEARCH_RADIUS = 5;
    public static final MultiblockShape MULTIBLOCK_SHAPE = new MultiblockShape(3, 6, 5, new BlockPos(1, 0, 0));

    public MinerBlock(Properties properties, long intervalTicks) {
        super(properties, intervalTicks);
    }

    @Override
    protected boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        boolean occupied = level.getBlockEntity(pos) instanceof ResourceNodeBlockEntity nodeBE && nodeBE.isOccupied();
        return !occupied && findNearbyResourceNode(level, pos) != null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos anchor = context.getClickedPos();

        BlockPos nodePos = findNearbyResourceNode(level, anchor);
        if (nodePos == null) {
            warnPlayer(context, Satiscraftory.MODID + ".invalid_placement_for_miner");
            return null;
        }

        if (level.getBlockEntity(nodePos) instanceof ResourceNodeBlockEntity nodeBE && nodeBE.isOccupied()) {
            warnPlayer(context, Satiscraftory.MODID + ".node_already_occupied");
            return null;
        }

        Direction facing = context.getHorizontalDirection().getOpposite();
        if (!MultiblockPlacement.canPlaceMultiblock(level, MULTIBLOCK_SHAPE, anchor, facing)) {
            warnPlayer(
                    context,
                    Satiscraftory.MODID + ".invalid_multiblock_placement",
                    MULTIBLOCK_SHAPE.width(), MULTIBLOCK_SHAPE.depth(), MULTIBLOCK_SHAPE.height()
            );
            return null;
        }

        return super.getStateForPlacement(context);
    }

    @Override
    public BlockState getPreviewPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        MultiblockPlacement.stampFillers(level, MULTIBLOCK_SHAPE, pos, facing, ManifoldBlocks.MULTIBLOCK_FILLER.get());
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);

        if (!MultiblockDemolition.isInProgress(level)) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            MultiblockDemolition.demolishFillers(level, MULTIBLOCK_SHAPE.absoluteFillerPositions(pos, facing));
        }
    }

    private static void warnPlayer(BlockPlaceContext context, String translationKey, Object... args) {
        if (!context.getLevel().isClientSide() && context.getPlayer() != null) {
            context.getPlayer().sendOverlayMessage(
                    Component.translatable(translationKey, args).withStyle(ChatFormatting.RED)
            );
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new MinerBlockEntity(SCBlockEntities.MINER.get(), pos, state);
    }

    public static @Nullable BlockPos findNearbyResourceNode(LevelReader level, BlockPos pos) {
        BlockPos min = pos.offset(-NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS);
        BlockPos max = pos.offset(NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS);
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(candidate).is(SCBlockTags.RESOURCE_NODES)) {
                return candidate.immutable();
            }
        }
        return null;
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected float getShadeBrightness(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        return 1f;
    }

    @Override
    protected boolean propagatesSkylightDown(@NonNull BlockState state) {
        return true;
    }

    @Override
    public MultiblockShape getMultiblockShape() {
        return MULTIBLOCK_SHAPE;
    }
}