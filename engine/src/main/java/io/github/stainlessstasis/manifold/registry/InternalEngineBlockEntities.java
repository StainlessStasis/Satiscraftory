package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block_entity.BeltBlockEntity;
import io.github.stainlessstasis.manifold.block_entity.ConsumerBlockEntity;
import io.github.stainlessstasis.manifold.block_entity.MachineBlockEntity;
import io.github.stainlessstasis.manifold.block_entity.ProducerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InternalEngineBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Manifold.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProducerBlockEntity>> PRODUCER =
            BLOCK_ENTITIES.register("producer", () -> new BlockEntityType<>(
                    ProducerBlockEntity::new, InternalEngineBlocks.PRODUCER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeltBlockEntity>> BELT =
            BLOCK_ENTITIES.register("belt", () -> new BlockEntityType<>(
                    BeltBlockEntity::new,
                    InternalEngineBlocks.BELT_MK1.get(),
                    InternalEngineBlocks.BELT_MK2.get()
                    )
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConsumerBlockEntity>> CONSUMER =
            BLOCK_ENTITIES.register("consumer", () -> new BlockEntityType<>(
                    ConsumerBlockEntity::new, InternalEngineBlocks.CONSUMER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>> MACHINE =
            BLOCK_ENTITIES.register("machine", () -> new BlockEntityType<>(
                    MachineBlockEntity::new, InternalEngineBlocks.MACHINE.get()));
}
