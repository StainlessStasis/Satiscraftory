package io.github.stainlessstasis.satiscraftory.world.resource_node;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.ResourceNodeType;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceNodeData extends SavedData {
    public static final SavedDataType<ResourceNodeData> TYPE = new SavedDataType<>(
            Satiscraftory.id("saved_resource_nodes"),
            ResourceNodeData::new,
            SavedResourceNode.CODEC.listOf().xmap(ResourceNodeData::fromList, ResourceNodeData::toList)
    );

    private final Map<GlobalPos, SavedResourceNode> nodes = new ConcurrentHashMap<>();

    public ResourceNodeData() {}

    private static ResourceNodeData fromList(List<SavedResourceNode> list) {
        ResourceNodeData data = new ResourceNodeData();
        for (SavedResourceNode node : list) {
            data.nodes.put(node.pos(), node);
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
        nodes.put(node.pos(), node);
        setDirty();
    }

    public SavedResourceNode getAt(GlobalPos pos) {
        return nodes.get(pos);
    }

    public List<SavedResourceNode> getNodesOfType(ResourceNodeType type) {
        return nodes.values().stream().filter(n -> n.type() == type).toList();
    }
}