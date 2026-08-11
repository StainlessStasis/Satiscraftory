package io.github.stainlessstasis.satiscraftory.item;

import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.manifold.util.MessageUtil;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCost;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCosts;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
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
        Identifier selectedId = BuiltInRegistries.ITEM.getKey(selected);

        BuildingCost cost = BuildingCosts.get(selectedId);
        System.out.println(selectedId);
        System.out.println(BuildingCosts.allCosts());
        System.out.println(cost);
        if (cost != null && !player.isCreative() && !hasRequiredItems(player, cost)) {
            MessageUtil.warnPlayer(player, Satiscraftory.MODID+".build_gun"+".missing_materials", selected.getDescriptionId());
            return InteractionResult.FAIL;
        }

        ItemStack dummyStack = new ItemStack(selected);
        BlockPlaceContext placeContext = new BlockPlaceContext(
                context.getLevel(),
                context.getPlayer(),
                context.getHand(),
                dummyStack,
                context.getHitResult()
        );

        InteractionResult result = selected.place(placeContext);
        if (result.consumesAction() && cost != null && !player.isCreative()) {
            consumeRequiredItems(player, cost);
        }
        return result;
    }

    public static boolean hasRequiredItems(Player player, BuildingCost cost) {
        Inventory inventory = player.getInventory();
        for (RecipeIngredient ingredient : cost.inputs()) {
            if (countHeld(inventory, ingredient.itemId()) < ingredient.amount()) {
                return false;
            }
        }
        return true;
    }

    public static void consumeRequiredItems(Player player, BuildingCost cost) {
        Inventory inventory = player.getInventory();
        for (RecipeIngredient ingredient : cost.inputs()) {
            removeHeld(inventory, ingredient.itemId(), ingredient.amount());
        }
    }

    public static int countHeld(Inventory inventory, Identifier itemId) {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null) return 0;

        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private static void removeHeld(Inventory inventory, Identifier itemId, int amount) {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null) return;

        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.is(item)) continue;

            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
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

    public static @Nullable BuildingCost getSelectedBuildingCost(Player player) {
        Identifier selectedId = BuiltInRegistries.ITEM.getKey(getSelectedBlockItem(player));
        return BuildingCosts.get(selectedId);
    }

    public static List<DeferredItem<BlockItem>> getPlaceableBlocks() {
        return PLACEABLE_BLOCKS;
    }
}