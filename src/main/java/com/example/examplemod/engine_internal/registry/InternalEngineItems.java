package com.example.examplemod.engine_internal.registry;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InternalEngineItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ExampleMod.MODID);

    public static final DeferredItem<BlockItem> PRODUCER =
            ITEMS.registerSimpleBlockItem("producer", InternalEngineBlocks.PRODUCER);
    public static final DeferredItem<BlockItem> BELT =
            ITEMS.registerSimpleBlockItem("belt", InternalEngineBlocks.BELT);
    public static final DeferredItem<BlockItem> CONSUMER =
            ITEMS.registerSimpleBlockItem("consumer", InternalEngineBlocks.CONSUMER);
    public static final DeferredItem<BlockItem> MACHINE =
            ITEMS.registerSimpleBlockItem("machine", InternalEngineBlocks.MACHINE);
}
