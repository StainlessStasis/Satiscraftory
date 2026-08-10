package io.github.stainlessstasis.satiscraftory.item;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuildGunItem extends Item {
    // TODO: eventually replace with progression based unlocks
    private static final List<DeferredItem<BlockItem>> PLACEABLE_BLOCKS = List.of(
            SCItems.MINER_MK1,
            SCItems.BELT_MK1,
            SCItems.BELT_MK2,
            SCItems.BELT_MK3,
            SCItems.POWER_POLE_MK1,
            SCItems.BIOMASS_BURNER,
            ManifoldItems.SPLITTER,
            ManifoldItems.MERGER,
            ManifoldItems.MACHINE,
            ManifoldItems.CONTAINER,
            ManifoldItems.CONSUMER,
            ManifoldItems.POWER_PRODUCER
    );

    private static final Map<UUID, Identifier> selectedBlockByPlayer = new HashMap<>();

    public BuildGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer) || !(context.getLevel() instanceof ServerLevel)) {
            return InteractionResult.PASS;
        }

        if (player.isCrouching()) {
            cycleSelection(player);
            return InteractionResult.SUCCESS_SERVER;
        }

        return placeSelected(context, player);
    }

    private InteractionResult placeSelected(UseOnContext context, Player player) {
        BlockItem selected = getSelectedBlockItem(player);

        ItemStack dummyStack = new ItemStack(selected);
        BlockPlaceContext placeContext = new BlockPlaceContext(
                context.getLevel(),
                context.getPlayer(),
                context.getHand(),
                dummyStack,
                context.getHitResult()
        );

        return selected.place(placeContext);
    }

    private static void cycleSelection(Player player) {
        UUID uuid = player.getUUID();
        Identifier current = selectedBlockByPlayer.get(uuid);

        int index = current == null ? -1 : indexOf(current);
        int nextIndex = (index + 1) % PLACEABLE_BLOCKS.size();

        BlockItem nextItem = PLACEABLE_BLOCKS.get(nextIndex).get();
        Identifier nextId = BuiltInRegistries.ITEM.getKey(nextItem);
        selectedBlockByPlayer.put(uuid, nextId);

        player.sendOverlayMessage(Component.translatable(nextItem.getDescriptionId()));
    }

    private static int indexOf(Identifier id) {
        for (int i = 0; i < PLACEABLE_BLOCKS.size(); i++) {
            if (BuiltInRegistries.ITEM.getKey(PLACEABLE_BLOCKS.get(i).get()).equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public static BlockItem getSelectedBlockItem(Player player) {
        Identifier selectedId = selectedBlockByPlayer.get(player.getUUID());
        if (selectedId != null) {
            int index = indexOf(selectedId);
            if (index != -1) {
                return PLACEABLE_BLOCKS.get(index).get();
            }
        }
        return PLACEABLE_BLOCKS.getFirst().get();
    }

    public static @Nullable Identifier getSelectedBlockId(Player player) {
        return selectedBlockByPlayer.get(player.getUUID());
    }

    public static List<DeferredItem<BlockItem>> getPlaceableBlocks() {
        return PLACEABLE_BLOCKS;
    }
}