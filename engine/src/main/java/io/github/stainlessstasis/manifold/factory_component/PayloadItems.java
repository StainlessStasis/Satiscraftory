package io.github.stainlessstasis.manifold.factory_component;

import io.github.stainlessstasis.manifold.Manifold;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class PayloadItems {
    private PayloadItems() {}

    public static String typeIdOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static @Nullable ItemStack toItemStack(String typeId, int count) {
        Identifier identifier = Identifier.parse(typeId);
        var optional = BuiltInRegistries.ITEM.getOptional(identifier);
        if (optional.isEmpty()) {
            Manifold.LOGGER.error("Could not convert Payload with ID: {} to an ItemStack; null.", typeId);
            return null;
        }
        return new ItemStack(optional.get(), count);
    }

    public static ItemStack toItemStack(Payload payload) {
        return toItemStack(payload.typeId(), payload.count());
    }

    public static Payload fromItemStack(ItemStack itemStack) {
        return new Payload(typeIdOf(itemStack.getItem()), itemStack.getCount());
    }
}
