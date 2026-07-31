package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.item.PowerLinkDebugItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
    public static final DeferredItem<PowerLinkDebugItem> POWER_LINK_DEBUG_ITEM = ITEMS.registerItem("power_link_debug", PowerLinkDebugItem::new);
}
