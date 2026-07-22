package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.registry.SCBlockTags;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
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
    }
}