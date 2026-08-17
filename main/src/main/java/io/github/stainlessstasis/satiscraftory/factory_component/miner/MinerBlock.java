package io.github.stainlessstasis.satiscraftory.factory_component.miner;

import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlock;
import io.github.stainlessstasis.manifold.item.power_link.PowerLinkItem;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.block.MultiblockUnfilledSets;
import io.github.stainlessstasis.satiscraftory.resource_node.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.block.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.block.SCBlockTags;
import io.github.stainlessstasis.manifold.util.MessageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MinerBlock extends ProducerBlock implements Multiblock<MinerBlock> {
    public static final int NODE_SEARCH_RADIUS = 5;
    public static final MultiblockShape MULTIBLOCK_SHAPE = new MultiblockShape(3, 7, 8, new BlockPos(1, 0, 0), MultiblockUnfilledSets.MINER);

    public MinerBlock(Properties properties, long intervalTicks) {
        super(properties, intervalTicks);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return type == SCBlockEntities.MINER.get()
                ? (_, pos, state, be) -> MinerBlockEntity.serverTick(serverLevel, pos, state, (MinerBlockEntity) be)
                : null;
    }

    @Override
    protected boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        boolean occupied = level.getBlockEntity(pos) instanceof ResourceNodeBlockEntity nodeBE && nodeBE.isOccupied();
        return !occupied && findNearbyResourceNode(level, pos) != null;
    }

    @Override
    public BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos anchor = context.getClickedPos();

        BlockPos nodePos = findNearbyResourceNode(level, anchor);
        if (nodePos == null) {
            MessageUtil.warnPlayer(context, Satiscraftory.MODID + ".invalid_placement_for_miner");
            return null;
        }

        if (level.getBlockEntity(nodePos) instanceof ResourceNodeBlockEntity nodeBE && nodeBE.isOccupied()) {
            MessageUtil.warnPlayer(context, Satiscraftory.MODID + ".node_already_occupied");
            return null;
        }

        return super.getStateForPlacement(context);
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
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos,
            @NonNull Player player, @NonNull BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MinerBlockEntity minerBE)) {
            return InteractionResult.PASS;
        }

        if (player.getMainHandItem().getItem() instanceof PowerLinkItem) {
            return InteractionResult.PASS;
        }

        player.openMenu(minerBE);
        return InteractionResult.CONSUME;
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