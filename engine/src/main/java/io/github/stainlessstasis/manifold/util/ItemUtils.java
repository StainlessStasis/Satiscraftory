package io.github.stainlessstasis.manifold.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class ItemUtils {
    private ItemUtils(){}

    public static Identifier idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
