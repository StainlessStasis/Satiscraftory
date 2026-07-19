package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Satiscraftory.MODID);

    public static final DeferredItem<BlockItem> BELT_MK1 =
            ITEMS.registerSimpleBlockItem("belt_mk1", SFBlocks.BELT_MK1);
    public static final DeferredItem<BlockItem> BELT_MK2 =
            ITEMS.registerSimpleBlockItem("belt_mk2", SFBlocks.BELT_MK2);
    public static final DeferredItem<BlockItem> BELT_MK3 =
            ITEMS.registerSimpleBlockItem("belt_mk3", SFBlocks.BELT_MK3);
}
