package io.github.stainlessstasis.satiscraftory.block;

import io.github.stainlessstasis.manifold.block.factory_component.ProducerBlock;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class MinerBlock extends ProducerBlock {
    public static final int NODE_SEARCH_RADIUS = 3;

    public MinerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        return findNearbyResourceNode(level, pos) != null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide() && context.getPlayer() != null) {
            if (findNearbyResourceNode(level, pos) == null) {
                context.getPlayer().sendOverlayMessage(
                        Component.translatable(Satiscraftory.MODID + ".invalid_placement_for_miner").withStyle(ChatFormatting.RED)
                );
                return null;
            }
        }

        return super.getStateForPlacement(context);
    }

    public static @Nullable BlockPos findNearbyResourceNode(LevelReader level, BlockPos pos) {
        BlockPos min = pos.offset(-NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS);
        BlockPos max = pos.offset(NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS);

        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(candidate).is(SCBlocks.RESOURCE_NODE.get())) {
                return candidate.immutable();
            }
        }
        return null;
    }

}
