package io.github.stainlessstasis.manifold.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemUtils {
    private ItemUtils(){}

    public static Identifier idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static int maxStackSizeFor(Identifier itemId) {
        return BuiltInRegistries.ITEM.getOptional(itemId).map(Item::getDefaultMaxStackSize).orElse(64);
    }

    public static void giveOrDrop(ServerPlayer player, Identifier itemId, int amount) {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null || amount <= 0) return;

        int maxStack = item.getDefaultMaxStackSize();
        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStack);
            ItemStack stack = new ItemStack(item, stackSize);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
            remaining -= stackSize;
        }
    }
}