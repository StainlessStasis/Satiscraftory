package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.manifold.block_entity.factory_component.ManifoldBlockEntityType;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block.ResourceNodeBlock;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Satiscraftory.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResourceNodeBlockEntity>> RESOURCE_NODE_MARKER =
            BLOCK_ENTITIES.register("resource_node_marker", () -> new ManifoldBlockEntityType<>(
                    ResourceNodeBlockEntity::new,
                    block -> block instanceof ResourceNodeBlock));
}
