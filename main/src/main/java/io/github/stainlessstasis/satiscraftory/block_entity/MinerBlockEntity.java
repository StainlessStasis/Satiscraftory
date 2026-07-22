package io.github.stainlessstasis.satiscraftory.block_entity;

import io.github.stainlessstasis.manifold.block.factory_component.ProducerBlock;
import io.github.stainlessstasis.manifold.block_entity.factory_component.ProducerBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.Producer;
import io.github.stainlessstasis.satiscraftory.block.MinerBlock;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MinerBlockEntity extends ProducerBlockEntity {
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