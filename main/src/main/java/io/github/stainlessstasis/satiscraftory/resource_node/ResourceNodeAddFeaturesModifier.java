package io.github.stainlessstasis.satiscraftory.resource_node;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.satiscraftory.SatiscraftoryConfig;
import io.github.stainlessstasis.satiscraftory.registry.world.SCBiomeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jspecify.annotations.NonNull;

public record ResourceNodeAddFeaturesModifier(
        HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step
) implements BiomeModifier {

    public static final MapCodec<ResourceNodeAddFeaturesModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ResourceNodeAddFeaturesModifier::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ResourceNodeAddFeaturesModifier::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ResourceNodeAddFeaturesModifier::step)
    ).apply(instance, ResourceNodeAddFeaturesModifier::new));

    @Override
    public void modify(@NonNull Holder<Biome> biome, @NonNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NonNull Builder builder) {
        if (phase != Phase.ADD) return;
        if (!SatiscraftoryConfig.GENERATE_RESOURCE_NODES.getAsBoolean()) return;
        if (!biomes.contains(biome)) return;

        var generationSettings = builder.getGenerationSettings();
        for (Holder<PlacedFeature> feature : features) {
            generationSettings.addFeature(step, feature);
        }
    }

    @Override
    public @NonNull MapCodec<? extends BiomeModifier> codec() {
        return SCBiomeModifiers.ADD_RESOURCE_NODE_FEATURES.get();
    }
}