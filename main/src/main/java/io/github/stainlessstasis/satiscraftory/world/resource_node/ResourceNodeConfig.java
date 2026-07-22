package io.github.stainlessstasis.satiscraftory.world.resource_node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ResourceNodeConfig(
        BlockState nodeState,
        BlockState resourceState,
        IntProvider radius,
        IntProvider clusterSize,
        IntProvider clusterSpread
) implements FeatureConfiguration {
    public static final Codec<ResourceNodeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("node_state").forGetter(ResourceNodeConfig::nodeState),
            BlockState.CODEC.fieldOf("resource_state").forGetter(ResourceNodeConfig::resourceState),
            IntProviders.NON_NEGATIVE_CODEC.fieldOf("radius").forGetter(ResourceNodeConfig::radius),
            IntProviders.NON_NEGATIVE_CODEC.fieldOf("clump_size").forGetter(ResourceNodeConfig::clusterSize),
            IntProviders.NON_NEGATIVE_CODEC.fieldOf("clump_spread").forGetter(ResourceNodeConfig::clusterSpread)
    ).apply(instance, ResourceNodeConfig::new));

    public ResourceNodeConfig withNodeState(BlockState nodeState) {
        return new ResourceNodeConfig(nodeState, resourceState, radius, clusterSize, clusterSpread);
    }

    public ResourceNodeConfig withResourceState(BlockState resourceState) {
        return new ResourceNodeConfig(nodeState, resourceState, radius, clusterSize, clusterSpread);
    }

    public ResourceNodeConfig withRadius(IntProvider radius) {
        return new ResourceNodeConfig(nodeState, resourceState, radius, clusterSize, clusterSpread);
    }

    public ResourceNodeConfig withClusterSize(IntProvider clusterSize) {
        return new ResourceNodeConfig(nodeState, resourceState, radius, clusterSize, clusterSpread);
    }

    public ResourceNodeConfig withClusterSpread(IntProvider clusterSpread) {
        return new ResourceNodeConfig(nodeState, resourceState, radius, clusterSize, clusterSpread);
    }
}