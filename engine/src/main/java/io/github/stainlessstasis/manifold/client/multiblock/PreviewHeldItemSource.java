package io.github.stainlessstasis.manifold.client.multiblock;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Extension point for previewing multiblocks based on a held item (used for build gun in SC)
 */
public final class PreviewHeldItemSource {
    private static final List<Function<Player, ItemStack>> RESOLVERS = new ArrayList<>();

    private PreviewHeldItemSource() {}

    public static void register(Function<Player, ItemStack> resolver) {
        RESOLVERS.add(resolver);
    }

    public static ItemStack resolve(Player player) {
        for (Function<Player, ItemStack> resolver : RESOLVERS) {
            ItemStack resolved = resolver.apply(player);
            if (!resolved.isEmpty()) {
                return resolved;
            }
        }
        return player.getMainHandItem();
    }
}