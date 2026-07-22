package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.registry.SCBlockTags;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SCBlockTagsProvider extends BlockTagsProvider {
    public SCBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        var builder = tag(SCBlockTags.RESOURCE_NODES);
        for (var type : SCResourceNodes.TYPES) {
            builder.add(type.getNodeBlock().get());
        }

        tag(SCBlockTags.RESOURCE_NODE_REPLACEABLE).add(
                Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT,
                Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM,
                Blocks.STONE, Blocks.DEEPSLATE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
                Blocks.SAND, Blocks.RED_SAND, Blocks.SANDSTONE, Blocks.RED_SANDSTONE,
                Blocks.GRAVEL, Blocks.TERRACOTTA, Blocks.CALCITE, Blocks.TUFF,
                Blocks.SNOW_BLOCK, Blocks.SNOW
        );
    }
}