package io.github.stainlessstasis.manifold.factory_component;

import io.github.stainlessstasis.manifold.Manifold;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class PayloadItems {
    private PayloadItems() {}

    // TODO: move this to its own util class
    public static Identifier idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static @Nullable ItemStack toItemStack(Identifier itemId, int count) {
        var optional = BuiltInRegistries.ITEM.getOptional(itemId);
        if (optional.isEmpty()) {
            Manifold.LOGGER.error("Could not convert Payload with ID: {} to an ItemStack; null.", itemId);
            return null;
        }
        return new ItemStack(optional.get(), count);
    }

    public static ItemStack toItemStack(Payload payload) {
        return toItemStack(payload.itemId(), payload.count());
    }

    public static Payload fromItemStack(ItemStack itemStack) {
        return new Payload(idOf(itemStack.getItem()), itemStack.getCount());
    }
}
