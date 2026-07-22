package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class SCBlockTags {
    public static final TagKey<Block> RESOURCE_NODES = TagKey.create(Registries.BLOCK, Satiscraftory.id("resource_nodes"));
}