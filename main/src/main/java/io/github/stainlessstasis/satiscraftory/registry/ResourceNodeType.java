package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block.ResourceNodeBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ResourceNodeType {
    private final String name;
    private final Block resourceBlock;
    private final IntProvider radius;
    private final int rarity;
    private final DeferredBlock<ResourceNodeBlock> nodeBlock;

    public ResourceNodeType(String name, Item resourceItem, Block resourceBlock, IntProvider radius, int rarity) {
        this.name = name;
        this.resourceBlock = resourceBlock;
        this.radius = radius;
        this.rarity = rarity;
        this.nodeBlock = SCBlocks.registerResourceNode(name, resourceItem);
    }

    public String name() { return name; }
    public Block resourceBlock() { return resourceBlock; }
    public IntProvider radius() { return radius; }
    public int rarity() { return rarity; }
    public DeferredBlock<ResourceNodeBlock> nodeBlock() { return nodeBlock; }

    public Identifier nodeId() {
        return Satiscraftory.id(name + "_node");
    }
}