package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.item.CableItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Satiscraftory.MODID);

    public static final DeferredItem<BlockItem> MINER_MK1 = ITEMS.registerSimpleBlockItem("miner_mk1", SCBlocks.MINER_MK1);
    public static final DeferredItem<BlockItem> BELT_MK1 = ITEMS.registerSimpleBlockItem("belt_mk1", SCBlocks.BELT_MK1);
    public static final DeferredItem<BlockItem> BELT_MK2 = ITEMS.registerSimpleBlockItem("belt_mk2", SCBlocks.BELT_MK2);
    public static final DeferredItem<BlockItem> BELT_MK3 = ITEMS.registerSimpleBlockItem("belt_mk3", SCBlocks.BELT_MK3);
    public static final DeferredItem<Item> IRON_PLATE = ITEMS.registerSimpleItem("iron_plate");
    public static final DeferredItem<Item> IRON_ROD = ITEMS.registerSimpleItem("iron_rod");
    public static final DeferredItem<Item> SCREWS = ITEMS.registerSimpleItem("screws");

    // TODO: replace this with the one from 0.4.1 merge whenever i do that
    public static final DeferredItem<Item> CABLE = ITEMS.registerItem(
            "cable",
            CableItem::new
    );
}
