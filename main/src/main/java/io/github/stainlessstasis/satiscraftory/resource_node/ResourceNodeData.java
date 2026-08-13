package io.github.stainlessstasis.satiscraftory.resource_node;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.world.ResourceNodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ResourceNodeData extends SavedData {
    public static final SavedDataType<ResourceNodeData> TYPE = new SavedDataType<>(
            Satiscraftory.id("saved_resource_nodes"),
            ResourceNodeData::new,
            SavedResourceNode.CODEC.listOf().xmap(ResourceNodeData::fromList, ResourceNodeData::toList)
    );

    private final Map<GlobalPos, SavedResourceNode> nodes = new ConcurrentHashMap<>();
    private final Map<ChunkKey, List<SavedResourceNode>> byChunk = new ConcurrentHashMap<>();

    public ResourceNodeData() {}

    private static ResourceNodeData fromList(List<SavedResourceNode> list) {
        ResourceNodeData data = new ResourceNodeData();
        for (SavedResourceNode node : list) {
            data.index(node);
        }
        return data;
    }

    private List<SavedResourceNode> toList() {
        return List.copyOf(nodes.values());
    }

    public static ResourceNodeData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void addRecord(SavedResourceNode node) {
        index(node);
        setDirty();
    }

    private void index(SavedResourceNode node) {
        nodes.put(node.pos(), node);
        ChunkKey key = ChunkKey.of(node.pos());
        byChunk.computeIfAbsent(key, _ -> new CopyOnWriteArrayList<>()).add(node);
    }

    public SavedResourceNode getAt(GlobalPos pos) {
        return nodes.get(pos);
    }

    public List<SavedResourceNode> getNodesOfType(ResourceNodeType type) {
        return nodes.values().stream().filter(node -> node.type() == type).toList();
    }

    public List<SavedResourceNode> findNearby(ResourceNodeType type, ResourceKey<Level> dimension, BlockPos origin, double range, int limit) {
        double rangeSqr = range * range;
        return nodes.values().stream()
                .filter(node -> node.type() == type)
                .filter(node -> node.pos().dimension().equals(dimension))
                .filter(node -> node.pos().pos().distSqr(origin) <= rangeSqr)
                .sorted(Comparator.comparingDouble(node -> node.pos().pos().distSqr(origin)))
                .limit(limit)
                .toList();
    }

    public List<SavedResourceNode> getNodesInChunk(ResourceKey<Level> dimension, ChunkPos chunkPos) {
        List<SavedResourceNode> found = byChunk.get(new ChunkKey(dimension, chunkPos.x(), chunkPos.z()));
        return found != null ? found : List.of();
    }

    private record ChunkKey(ResourceKey<Level> dimension, int x, int z) {
        static ChunkKey of(GlobalPos pos) {
            ChunkPos chunk = ChunkPos.containing(pos.pos());
            return new ChunkKey(pos.dimension(), chunk.x(), chunk.z());
        }
    }
}