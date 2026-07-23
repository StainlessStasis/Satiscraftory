package io.github.stainlessstasis.satiscraftory.block_entity;

import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlock;
import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.satiscraftory.block.MinerBlock;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MinerBlockEntity extends ProducerBlockEntity implements MultiblockControllerAccess {
    private @Nullable BlockPos linkedNodePos = null;

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        super(SCBlockEntities.MINER.get(), pos, state);
    }

    public MinerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            linkToResourceNode(serverLevel);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            unlinkFromResourceNode(serverLevel);
        }
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return MinerBlock.MULTIBLOCK_SHAPE.absoluteFillerPositions(getBlockPos(), facing);
    }

    private void linkToResourceNode(ServerLevel level) {
        BlockPos nodePos = MinerBlock.findNearbyResourceNode(level, getBlockPos());
        if (nodePos == null) return;
        if (!(level.getBlockEntity(nodePos) instanceof ResourceNodeBlockEntity nodeBE)) return;
        if (!nodeBE.tryAssignMiner(getBlockPos())) return;

        linkedNodePos = nodePos.immutable();
        Producer producer = getProducer();
        if (producer == null) return;

        producer.setItemId(nodeBE.getResourceType());
        long baseInterval = getBlockState().getBlock() instanceof ProducerBlock producerBlock
                ? producerBlock.getIntervalTicks()
                : Producer.DEFAULT_INTERVAL_TICKS;
        float multiplier = nodeBE.getPurity().getProductionRateMultiplier();
        long effectiveInterval = Math.max(1, Math.round(baseInterval / multiplier));
        producer.setInterval(effectiveInterval);
    }

    public void unlinkFromResourceNode(ServerLevel level) {
        if (linkedNodePos == null) return;
        if (level.getBlockEntity(linkedNodePos) instanceof ResourceNodeBlockEntity nodeBE) {
            nodeBE.unassignMiner(getBlockPos());
        }
        linkedNodePos = null;
    }
}