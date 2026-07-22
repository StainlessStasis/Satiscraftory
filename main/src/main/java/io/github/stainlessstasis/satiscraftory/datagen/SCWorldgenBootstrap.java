package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.SCFeatures;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.world.feature.ResourceNodeConfig;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class SCWorldgenBootstrap {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, SCWorldgenBootstrap::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, SCWorldgenBootstrap::bootstrapPlacedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SCWorldgenBootstrap::bootstrapBiomeModifiers);

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(ResourceNodeType type) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, type.getNodeId());
    }

    private static ResourceKey<PlacedFeature> placedKey(ResourceNodeType type) {
        return ResourceKey.create(Registries.PLACED_FEATURE, type.getNodeId());
    }

    private static ResourceKey<BiomeModifier> modifierKey(ResourceNodeType type) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                Satiscraftory.id("add_" + type.getName() + "_node"));
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        for (ResourceNodeType type : SCResourceNodes.TYPES) {
            context.register(
                    configuredKey(type),
                    new ConfiguredFeature<>(
                            SCFeatures.RESOURCE_NODE.get(),
                            new ResourceNodeConfig(
                                    type.getNodeBlock().get().defaultBlockState(),
                                    type.getResourceBlock().defaultBlockState(),
                                    type.getRadius(), type.getClusterSize(), type.getClusterSpread()
                            )
                    )
            );
        }
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        for (ResourceNodeType type : SCResourceNodes.TYPES) {
            List<PlacementModifier> placement = List.of(
                    RarityFilter.onAverageOnceEvery(type.getRarity()),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                    BiomeFilter.biome()
            );
            context.register(
                    placedKey(type),
                    new PlacedFeature(configuredFeatures.getOrThrow(configuredKey(type)), placement)
            );
        }
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        for (ResourceNodeType type : SCResourceNodes.TYPES) {
            context.register(
                    modifierKey(type),
                    new BiomeModifiers.AddFeaturesBiomeModifier(
                            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                            HolderSet.direct(placedFeatures.getOrThrow(placedKey(type))),
                            GenerationStep.Decoration.SURFACE_STRUCTURES
                    )
            );
        }
    }
}