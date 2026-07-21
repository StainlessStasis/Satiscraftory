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
    public static final DeferredItem<BlockItem> CONSUMER =
            ITEMS.registerSimpleBlockItem("consumer", ManifoldBlocks.CONSUMER);
    public static final DeferredItem<BlockItem> MACHINE =
            ITEMS.registerSimpleBlockItem("machine", ManifoldBlocks.MACHINE);
    public static final DeferredItem<BlockItem> CONTAINER =
            ITEMS.registerSimpleBlockItem("container", ManifoldBlocks.CONTAINER);
    public static final DeferredItem<BlockItem> SPLITTER =
            ITEMS.registerSimpleBlockItem("splitter", ManifoldBlocks.SPLITTER);
    public static final DeferredItem<BlockItem> MERGER =
            ITEMS.registerSimpleBlockItem("merger", ManifoldBlocks.MERGER);
}
