package io.github.stainlessstasis.satiscraftory.building;

import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.util.MessageUtil;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.SatiscraftoryConfig;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BuildGunItem extends Item {
    private static final Map<UUID, Identifier> selectedBlockByPlayer = new HashMap<>();

    public BuildGunItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer) || !(context.getLevel() instanceof ServerLevel)) {
            return InteractionResult.SUCCESS; // success is intended to be here so client doesnt open block menus
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
        boolean buildingCostsEnabled = SatiscraftoryConfig.BUILDING_COSTS.getAsBoolean();

        boolean canPlace = !buildingCostsEnabled || cost == null || hasRequiredItems(player, cost);

        if (!canPlace) {
            MessageUtil.warnPlayer(player, Satiscraftory.MODID+".build_gun.missing_materials", Component.translatable(selected.getDescriptionId()));
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
        if (cost != null && result.consumesAction() && !player.isCreative() && buildingCostsEnabled) {
            consumeRequiredItems(player, cost);
        }

        if (result.consumesAction()) {
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.CONSUME;
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
        var buildingEntries = BuildingCatalog.allForCurrentTier(player.level());

        int index = current == null ? -1 : indexOf(current);
        int nextIndex = (index + 1) % buildingEntries.size();

        Identifier nextId = buildingEntries.get(nextIndex).id();
        selectedBlockByPlayer.put(uuid, nextId);
    }

    private static int indexOf(Identifier id) {
        var buildingEntries = BuildingCatalog.all();
        for (int i = 0; i < buildingEntries.size(); i++) {
            if (buildingEntries.get(i).id().equals(id)) return i;
        }
        return -1;
    }

    public static void setSelectedBlock(Player player, Identifier buildingItemId) {
        selectedBlockByPlayer.put(player.getUUID(), buildingItemId);
    }

    public static BlockItem getSelectedBlockItem(Player player) {
        Identifier selectedId = selectedBlockByPlayer.get(player.getUUID());
        if (selectedId != null) {
            BuildingCatalog.BuildingEntry entry = BuildingCatalog.byId(selectedId).orElse(null);
            if (entry != null) return entry.blockItem();
        }
        return BuildingCatalog.getFirst().blockItem();
    }

    public static @Nullable Identifier getSelectedBlockId(Player player) {
        return selectedBlockByPlayer.get(player.getUUID());
    }

    public static @Nullable BuildingCost getSelectedBuildingCost(Player player) {
        Identifier selectedId = BuiltInRegistries.ITEM.getKey(getSelectedBlockItem(player));
        return BuildingCosts.get(selectedId);
    }
}