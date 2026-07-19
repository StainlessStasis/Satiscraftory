package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block.ContainerBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.ConsumerBlock;
import io.github.stainlessstasis.manifold.block.MachineBlock;
import io.github.stainlessstasis.manifold.block.ProducerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ManifoldBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Manifold.MODID);

    public static final DeferredBlock<ProducerBlock> PRODUCER = BLOCKS.registerBlock("producer",
            ProducerBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops());

    public static final DeferredBlock<ConsumerBlock> CONSUMER = BLOCKS.registerBlock("consumer",
            ConsumerBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops());

    public static final DeferredBlock<MachineBlock> MACHINE = BLOCKS.registerBlock("machine",
            MachineBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops());

    public static final DeferredBlock<ContainerBlock> CONTAINER = BLOCKS.registerBlock("container",
            ContainerBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops());
}
