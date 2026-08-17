package io.github.stainlessstasis.manifold.client.block_preview;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Extension point for previewing blocks/multiblocks based on a held item (used for build gun in SC)
 */
public final class PreviewHeldItemSource {
    private static final List<Function<Player, ItemStack>> RESOLVERS = new ArrayList<>();

    private PreviewHeldItemSource() {}

    public static void register(Function<Player, ItemStack> resolver) {
        RESOLVERS.add(resolver);
    }

    /**
     * @param stack the item to preview placement for
     * @param fromOverride true if {@code stack} was substituted by a registered resolver
     * (e.g. a build-gun style tool), false if it's the player's actual mainhand item
     */
    public record Resolved(ItemStack stack, boolean fromOverride) {}

    public static Resolved resolve(Player player) {
        for (Function<Player, ItemStack> resolver : RESOLVERS) {
            ItemStack resolved = resolver.apply(player);
            if (!resolved.isEmpty()) {
                return new Resolved(resolved, true);
            }
        }
        return new Resolved(player.getMainHandItem(), false);
    }

}