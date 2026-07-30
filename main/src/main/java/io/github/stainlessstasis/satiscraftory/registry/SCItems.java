package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SCItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Satiscraftory.MODID);

    private static final List<DeferredHolder<Item, BlockItem>> FACTORY_BLOCK_ITEMS = new ArrayList<>();
    private static final List<DeferredHolder<Item, Item>> FACTORY_ITEMS = new ArrayList<>();

    public static List<DeferredHolder<Item, BlockItem>> getFactoryBlockItems() {
        return Collections.unmodifiableList(FACTORY_BLOCK_ITEMS);
    }
    public static List<DeferredHolder<Item, Item>> getFactoryItems() {
        return Collections.unmodifiableList(FACTORY_ITEMS);
    }

    public static final DeferredItem<BlockItem> MINER_MK1 = registerFactoryBlockItem("miner_mk1", SCBlocks.MINER_MK1);
    public static final DeferredItem<BlockItem> BELT_MK1 = registerFactoryBlockItem("belt_mk1", SCBlocks.BELT_MK1);
    public static final DeferredItem<BlockItem> BELT_MK2 = registerFactoryBlockItem("belt_mk2", SCBlocks.BELT_MK2);
    public static final DeferredItem<BlockItem> BELT_MK3 = registerFactoryBlockItem("belt_mk3", SCBlocks.BELT_MK3);

    public static final DeferredItem<Item> IRON_PLATE = registerFactoryItem("iron_plate");
    public static final DeferredItem<Item> IRON_ROD = registerFactoryItem("iron_rod");
    public static final DeferredItem<Item> SCREWS = registerFactoryItem("screws");
    public static final DeferredItem<Item> COPPER_SHEET = registerFactoryItem("copper_sheet");

    public static <B extends Block> DeferredItem<BlockItem> registerFactoryBlockItem(String name, Supplier<B> blockSupplier) {
        DeferredItem<BlockItem> item = ITEMS.registerSimpleBlockItem(name, blockSupplier);
        FACTORY_BLOCK_ITEMS.add(item);
        return item;
    }

    public static DeferredItem<Item> registerFactoryItem(String name) {
        DeferredItem<Item> item = ITEMS.registerSimpleItem(name);
        FACTORY_ITEMS.add(item);
        return item;
    }
}
