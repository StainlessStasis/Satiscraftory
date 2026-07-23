package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block.factory_component.*;
import io.github.stainlessstasis.manifold.block.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block_entity.factory_component.*;
import io.github.stainlessstasis.manifold.block.MultiblockFillerBlock;
import io.github.stainlessstasis.manifold.block_entity.MultiblockFillerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ManifoldBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Manifold.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProducerBlockEntity>> PRODUCER =
            BLOCK_ENTITIES.register("producer", () -> new ManifoldBlockEntityType<>(
                    ProducerBlockEntity::new,
                    block -> block instanceof ProducerBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeltBlockEntity>> BELT =
            BLOCK_ENTITIES.register("belt", () -> new ManifoldBlockEntityType<>(
                    BeltBlockEntity::new,
                    block -> block instanceof BeltBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConsumerBlockEntity>> CONSUMER =
            BLOCK_ENTITIES.register("consumer", () -> new ManifoldBlockEntityType<>(
                    ConsumerBlockEntity::new,
                    block -> block instanceof ConsumerBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>> MACHINE =
            BLOCK_ENTITIES.register("machine", () -> new ManifoldBlockEntityType<>(
                    MachineBlockEntity::new,
                    block -> block instanceof MachineBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContainerBlockEntity>> CONTAINER =
            BLOCK_ENTITIES.register("container", () -> new ManifoldBlockEntityType<>(
                    ContainerBlockEntity::new,
                    block -> block instanceof ContainerBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SplitterBlockEntity>> SPLITTER =
            BLOCK_ENTITIES.register("splitter", () -> new ManifoldBlockEntityType<>(
                    SplitterBlockEntity::new,
                    block -> block instanceof SplitterBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MergerBlockEntity>> MERGER =
            BLOCK_ENTITIES.register("merger", () -> new ManifoldBlockEntityType<>(
                    MergerBlockEntity::new,
                    block -> block instanceof MergerBlock));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockFillerBlockEntity>> MULTIBLOCK_FILLER =
            BLOCK_ENTITIES.register("multiblock_filler", () -> new ManifoldBlockEntityType<>(
                    MultiblockFillerBlockEntity::new,
                    block -> block instanceof MultiblockFillerBlock));
}
