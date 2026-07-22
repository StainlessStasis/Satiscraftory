package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block.ResourceNodeBlock;
import io.github.stainlessstasis.satiscraftory.world.resource_node.ResourceNodeConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ResourceNodeType {
    private final String name;
    private final Block resourceBlock;
    private final IntProvider radius;
    private final IntProvider clusterSize;
    private final IntProvider clusterSpread;
    private final int rarity;
    private final DeferredBlock<ResourceNodeBlock> nodeBlock;

    public ResourceNodeType(
            String name, Item resourceItem, Block resourceBlock,
            IntProvider radius, IntProvider clusterSize, IntProvider clusterSpread, int rarity
    ) {
        this.name = name;
        this.resourceBlock = resourceBlock;
        this.radius = radius;
        this.clusterSize = clusterSize;
        this.clusterSpread = clusterSpread;
        this.rarity = rarity;
        this.nodeBlock = SCBlocks.registerResourceNode(name, resourceItem);
    }

    public String getName() { return name; }
    public Block getResourceBlock() { return resourceBlock; }
    public IntProvider getRadius() { return radius; }
    public IntProvider getClusterSize() { return clusterSize; }
    public IntProvider getClusterSpread() { return clusterSpread; }
    public int getRarity() { return rarity; }
    public DeferredBlock<ResourceNodeBlock> getNodeBlock() { return nodeBlock; }
    public Identifier getNodeId() {
        return Satiscraftory.id(name + "_node");
    }
    public ResourceNodeConfig toConfig() {
        return new ResourceNodeConfig(
                getNodeBlock().get().defaultBlockState(), getResourceBlock().defaultBlockState(),
                getRadius(), getClusterSize(), getClusterSpread()
        );
    }
}