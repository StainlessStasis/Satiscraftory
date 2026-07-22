package io.github.stainlessstasis.satiscraftory.block;

import io.github.stainlessstasis.manifold.block.factory_component.ProducerBlock;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block_entity.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockTags;
import io.github.stainlessstasis.satiscraftory.registry.SCBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MinerBlock extends ProducerBlock {
    public static final int NODE_SEARCH_RADIUS = 3;

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
        BlockPos pos = context.getClickedPos();

        BlockPos nodePos = findNearbyResourceNode(level, pos);
        if (nodePos == null) {
            warnPlayer(context, Satiscraftory.MODID + ".invalid_placement_for_miner");
            return null;
        }

        if (level.getBlockEntity(nodePos) instanceof ResourceNodeBlockEntity nodeBE && nodeBE.isOccupied()) {
            warnPlayer(context, Satiscraftory.MODID + ".node_already_occupied");
            return null;
        }

        return super.getStateForPlacement(context);
    }

    private static void warnPlayer(BlockPlaceContext context, String translationKey) {
        if (!context.getLevel().isClientSide() && context.getPlayer() != null) {
            context.getPlayer().sendOverlayMessage(
                    Component.translatable(translationKey).withStyle(ChatFormatting.RED)
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
}