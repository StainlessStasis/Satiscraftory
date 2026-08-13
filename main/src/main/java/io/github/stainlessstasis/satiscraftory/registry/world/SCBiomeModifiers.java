package io.github.stainlessstasis.satiscraftory.registry.world;

import com.mojang.serialization.MapCodec;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.resource_node.ResourceNodeAddFeaturesModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class SCBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Satiscraftory.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ResourceNodeAddFeaturesModifier>> ADD_RESOURCE_NODE_FEATURES =
            BIOME_MODIFIER_SERIALIZERS.register("resource_node_add_features", () -> ResourceNodeAddFeaturesModifier.CODEC);
}