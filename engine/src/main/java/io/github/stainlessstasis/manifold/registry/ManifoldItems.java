package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ManifoldItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Manifold.MODID);

    public static final DeferredItem<BlockItem> PRODUCER =
            ITEMS.registerSimpleBlockItem("producer", ManifoldBlocks.PRODUCER);
    public static final DeferredItem<BlockItem> BELT_MK1 =
            ITEMS.registerSimpleBlockItem("belt_mk1", ManifoldBlocks.BELT_MK1);
    public static final DeferredItem<BlockItem> BELT_MK2 =
            ITEMS.registerSimpleBlockItem("belt_mk2", ManifoldBlocks.BELT_MK2);
    public static final DeferredItem<BlockItem> CONSUMER =
            ITEMS.registerSimpleBlockItem("consumer", ManifoldBlocks.CONSUMER);
    public static final DeferredItem<BlockItem> MACHINE =
            ITEMS.registerSimpleBlockItem("machine", ManifoldBlocks.MACHINE);
}
