package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCBlocks;
import io.github.stainlessstasis.satiscraftory.registry.SCFeatures;
import io.github.stainlessstasis.satiscraftory.world.feature.ResourceNodeConfig;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class SCWorldgenBootstrap {

    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_NODE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Satiscraftory.id("iron_node")
    );

    public static final ResourceKey<PlacedFeature> IRON_NODE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE,
            Satiscraftory.id("iron_node")
    );

    public static final ResourceKey<BiomeModifier> ADD_IRON_NODE = ResourceKey.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            Satiscraftory.id("add_iron_node")
    );

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, SCWorldgenBootstrap::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, SCWorldgenBootstrap::bootstrapPlacedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, SCWorldgenBootstrap::bootstrapBiomeModifiers);

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(
                IRON_NODE,
                new ConfiguredFeature<>(
                        SCFeatures.RESOURCE_NODE.get(),
                        new ResourceNodeConfig(
                                SCBlocks.IRON_RESOURCE_NODE.get().defaultBlockState(),
                                Blocks.IRON_ORE.defaultBlockState(),
                                UniformInt.of(3, 5)
                        )
                )
        );
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        List<PlacementModifier> placement = List.of(
                RarityFilter.onAverageOnceEvery(200),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        context.register(
                IRON_NODE_PLACED,
                new PlacedFeature(configuredFeatures.getOrThrow(IRON_NODE), placement)
        );
    }

    private static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(
                ADD_IRON_NODE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(IRON_NODE_PLACED)),
                        GenerationStep.Decoration.SURFACE_STRUCTURES
                )
        );
    }
}