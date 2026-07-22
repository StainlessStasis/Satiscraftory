package io.github.stainlessstasis.satiscraftory.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ResourceNodeConfig(BlockState markerState, BlockState resourceState, IntProvider radius) implements FeatureConfiguration {
    public static final Codec<ResourceNodeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("marker_state").forGetter(ResourceNodeConfig::markerState),
            BlockState.CODEC.fieldOf("resource_state").forGetter(ResourceNodeConfig::resourceState),
            IntProviders.NON_NEGATIVE_CODEC.fieldOf("radius").forGetter(ResourceNodeConfig::radius)
    ).apply(instance, ResourceNodeConfig::new));
}