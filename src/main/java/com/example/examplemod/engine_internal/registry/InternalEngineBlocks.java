package com.example.examplemod.engine_internal.registry;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.engine_internal.block.BeltBlock;
import com.example.examplemod.engine_internal.block.ConsumerBlock;
import com.example.examplemod.engine_internal.block.MachineBlock;
import com.example.examplemod.engine_internal.block.ProducerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InternalEngineBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(ExampleMod.MODID);

    public static final DeferredBlock<ProducerBlock> PRODUCER = BLOCKS.register("producer",
            () -> new ProducerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<BeltBlock> BELT = BLOCKS.register("belt",
            () -> new BeltBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(2.0f)));

    public static final DeferredBlock<ConsumerBlock> CONSUMER = BLOCKS.register("consumer",
            () -> new ConsumerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<MachineBlock> MACHINE = BLOCKS.register("machine",
            () -> new MachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops()));
}
