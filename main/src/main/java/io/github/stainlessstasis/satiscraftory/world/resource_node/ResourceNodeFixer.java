package io.github.stainlessstasis.satiscraftory.world.resource_node;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.ResourceNodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = Satiscraftory.MODID)
public class ResourceNodeFixer {
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        LevelChunk chunk = event.getChunk();
        ChunkPos chunkPos = chunk.getPos();
        ResourceNodeData data = ResourceNodeData.get(serverLevel);
        var cachedNodes = data.getNodesInChunk(serverLevel.dimension(), chunkPos);
        if (cachedNodes.isEmpty()) return;

        for (SavedResourceNode node : cachedNodes) {
            BlockPos pos = node.pos().pos();
            ResourceNodeType type = node.type();
            Block expectedBlock = type.getNodeBlock().get();

            if (serverLevel.getBlockState(pos).getBlock() != expectedBlock) {
                Satiscraftory.LOGGER.warn("Repairing missing/mismatched resource node at {} (expected {})", pos, type.getName());
                serverLevel.setBlock(pos, expectedBlock.defaultBlockState(), Block.UPDATE_ALL);
                if (serverLevel.getBlockEntity(pos) instanceof ResourceNodeBlockEntity nodeBE) {
                    nodeBE.setPurity(node.purity());
                }
            }
        }
    }
}