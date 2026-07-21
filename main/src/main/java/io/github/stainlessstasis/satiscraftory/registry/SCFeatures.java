package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.world.feature.ResourceNodeConfig;
import io.github.stainlessstasis.satiscraftory.world.feature.ResourceNodeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SCFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Satiscraftory.MODID);

    public static final DeferredHolder<Feature<?>, ResourceNodeFeature> RESOURCE_NODE =
            FEATURES.register("resource_node", () -> new ResourceNodeFeature(ResourceNodeConfig.CODEC));
}

