package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class InternalEngineItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Manifold.MODID);

    public static final DeferredItem<BlockItem> PRODUCER =
            ITEMS.registerSimpleBlockItem("producer", InternalEngineBlocks.PRODUCER);
    public static final DeferredItem<BlockItem> BELT_MK1 =
            ITEMS.registerSimpleBlockItem("belt_mk1", InternalEngineBlocks.BELT_MK1);
    public static final DeferredItem<BlockItem> BELT_MK2 =
            ITEMS.registerSimpleBlockItem("belt_mk2", InternalEngineBlocks.BELT_MK2);
    public static final DeferredItem<BlockItem> CONSUMER =
            ITEMS.registerSimpleBlockItem("consumer", InternalEngineBlocks.CONSUMER);
    public static final DeferredItem<BlockItem> MACHINE =
            ITEMS.registerSimpleBlockItem("machine", InternalEngineBlocks.MACHINE);
}
