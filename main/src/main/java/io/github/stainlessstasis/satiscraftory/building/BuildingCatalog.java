package io.github.stainlessstasis.satiscraftory.building;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlock;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlockData;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlocks;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class BuildingCatalog {
    private static final List<BuildingEntry> ENTRIES = buildEntries();
    private static final Map<BuildingCategory, List<BuildingEntry>> BY_CATEGORY = buildCategoryIndex();

    private BuildingCatalog() {}

    private static List<BuildingEntry> buildEntries() {
        List<BuildingEntry> entries = new ArrayList<>(List.of(
                new BuildingEntry(SCItems.MINER_MK1, BuildingCategory.PRODUCTION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(SCItems.BELT_MK1, BuildingCategory.LOGISTICS, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(SCItems.BELT_MK2, BuildingCategory.LOGISTICS, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(SCItems.BELT_MK3, BuildingCategory.LOGISTICS, 999, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(SCItems.POWER_POLE_MK1, BuildingCategory.POWER, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(SCItems.BIOMASS_BURNER, BuildingCategory.POWER, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(ManifoldItems.SPLITTER, BuildingCategory.LOGISTICS, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(ManifoldItems.MERGER, BuildingCategory.LOGISTICS, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(ManifoldItems.MACHINE, BuildingCategory.PRODUCTION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(ManifoldItems.CONTAINER, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(ManifoldItems.CONSUMER, BuildingCategory.LOGISTICS, 1, TierUnlock.UNLOCKED_BY_DEFAULT),

                // random shit for testing
                new BuildingEntry(() -> Items.OAK_DOOR, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.LECTERN, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.IRON_CHAIN, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.IRON_BLOCK, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.CHEST, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.HOPPER, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.QUARTZ_STAIRS, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.LIGHTNING_ROD, BuildingCategory.ORGANIZATION, 2, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.BROWN_BANNER, BuildingCategory.ORGANIZATION, 3, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.NETHERITE_BLOCK, BuildingCategory.ORGANIZATION, 4, TierUnlock.UNLOCKED_BY_DEFAULT),
                new BuildingEntry(() -> Items.BEDROCK, BuildingCategory.ORGANIZATION, 5, TierUnlock.UNLOCKED_BY_DEFAULT)
        ));
        addRandomBullshit(entries);
        return List.copyOf(entries);
    }

    private static void addRandomBullshit(List<BuildingEntry> entries) {
        Supplier<?>[] blocks = new Supplier<?>[]{
                () -> Items.OAK_LOG, () -> Items.OAK_WOOD, () -> Items.STRIPPED_OAK_LOG, () -> Items.STRIPPED_OAK_WOOD,
                () -> Items.OAK_PLANKS, () -> Items.OAK_STAIRS, () -> Items.OAK_SLAB, () -> Items.OAK_FENCE,
                () -> Items.OAK_FENCE_GATE, () -> Items.OAK_TRAPDOOR, () -> Items.OAK_PRESSURE_PLATE, () -> Items.OAK_BUTTON,

                () -> Items.SPRUCE_LOG, () -> Items.SPRUCE_WOOD, () -> Items.STRIPPED_SPRUCE_LOG, () -> Items.STRIPPED_SPRUCE_WOOD,
                () -> Items.SPRUCE_PLANKS, () -> Items.SPRUCE_STAIRS, () -> Items.SPRUCE_SLAB, () -> Items.SPRUCE_FENCE,
                () -> Items.SPRUCE_FENCE_GATE, () -> Items.SPRUCE_DOOR, () -> Items.SPRUCE_TRAPDOOR, () -> Items.SPRUCE_PRESSURE_PLATE, () -> Items.SPRUCE_BUTTON,

                () -> Items.BIRCH_LOG, () -> Items.BIRCH_WOOD, () -> Items.STRIPPED_BIRCH_LOG, () -> Items.STRIPPED_BIRCH_WOOD,
                () -> Items.BIRCH_PLANKS, () -> Items.BIRCH_STAIRS, () -> Items.BIRCH_SLAB, () -> Items.BIRCH_FENCE,
                () -> Items.BIRCH_FENCE_GATE, () -> Items.BIRCH_DOOR, () -> Items.BIRCH_TRAPDOOR, () -> Items.BIRCH_PRESSURE_PLATE, () -> Items.BIRCH_BUTTON,

                () -> Items.JUNGLE_LOG, () -> Items.JUNGLE_WOOD, () -> Items.STRIPPED_JUNGLE_LOG, () -> Items.STRIPPED_JUNGLE_WOOD,
                () -> Items.JUNGLE_PLANKS, () -> Items.JUNGLE_STAIRS, () -> Items.JUNGLE_SLAB, () -> Items.JUNGLE_FENCE,
                () -> Items.JUNGLE_FENCE_GATE, () -> Items.JUNGLE_DOOR, () -> Items.JUNGLE_TRAPDOOR, () -> Items.JUNGLE_PRESSURE_PLATE, () -> Items.JUNGLE_BUTTON,

                () -> Items.ACACIA_LOG, () -> Items.ACACIA_WOOD, () -> Items.STRIPPED_ACACIA_LOG, () -> Items.STRIPPED_ACACIA_WOOD,
                () -> Items.ACACIA_PLANKS, () -> Items.ACACIA_STAIRS, () -> Items.ACACIA_SLAB, () -> Items.ACACIA_FENCE,
                () -> Items.ACACIA_FENCE_GATE, () -> Items.ACACIA_DOOR, () -> Items.ACACIA_TRAPDOOR, () -> Items.ACACIA_PRESSURE_PLATE, () -> Items.ACACIA_BUTTON,

                () -> Items.DARK_OAK_LOG, () -> Items.DARK_OAK_WOOD, () -> Items.STRIPPED_DARK_OAK_LOG, () -> Items.STRIPPED_DARK_OAK_WOOD,
                () -> Items.DARK_OAK_PLANKS, () -> Items.DARK_OAK_STAIRS, () -> Items.DARK_OAK_SLAB, () -> Items.DARK_OAK_FENCE,
                () -> Items.DARK_OAK_FENCE_GATE, () -> Items.DARK_OAK_DOOR, () -> Items.DARK_OAK_TRAPDOOR, () -> Items.DARK_OAK_PRESSURE_PLATE, () -> Items.DARK_OAK_BUTTON,

                () -> Items.MANGROVE_LOG, () -> Items.MANGROVE_WOOD, () -> Items.STRIPPED_MANGROVE_LOG, () -> Items.STRIPPED_MANGROVE_WOOD,
                () -> Items.MANGROVE_PLANKS, () -> Items.MANGROVE_STAIRS, () -> Items.MANGROVE_SLAB, () -> Items.MANGROVE_FENCE,
                () -> Items.MANGROVE_FENCE_GATE, () -> Items.MANGROVE_DOOR, () -> Items.MANGROVE_TRAPDOOR, () -> Items.MANGROVE_PRESSURE_PLATE, () -> Items.MANGROVE_BUTTON,
                () -> Items.MANGROVE_ROOTS, () -> Items.MUDDY_MANGROVE_ROOTS,

                () -> Items.CHERRY_LOG, () -> Items.CHERRY_WOOD, () -> Items.STRIPPED_CHERRY_LOG, () -> Items.STRIPPED_CHERRY_WOOD,
                () -> Items.CHERRY_PLANKS, () -> Items.CHERRY_STAIRS, () -> Items.CHERRY_SLAB, () -> Items.CHERRY_FENCE,
                () -> Items.CHERRY_FENCE_GATE, () -> Items.CHERRY_DOOR, () -> Items.CHERRY_TRAPDOOR, () -> Items.CHERRY_PRESSURE_PLATE, () -> Items.CHERRY_BUTTON,

                () -> Items.BAMBOO_BLOCK, () -> Items.STRIPPED_BAMBOO_BLOCK, () -> Items.BAMBOO_PLANKS, () -> Items.BAMBOO_MOSAIC,
                () -> Items.BAMBOO_STAIRS, () -> Items.BAMBOO_MOSAIC_STAIRS, () -> Items.BAMBOO_SLAB, () -> Items.BAMBOO_MOSAIC_SLAB,
                () -> Items.BAMBOO_FENCE, () -> Items.BAMBOO_FENCE_GATE, () -> Items.BAMBOO_DOOR, () -> Items.BAMBOO_TRAPDOOR,
                () -> Items.BAMBOO_PRESSURE_PLATE, () -> Items.BAMBOO_BUTTON,

                () -> Items.CRIMSON_STEM, () -> Items.CRIMSON_HYPHAE, () -> Items.STRIPPED_CRIMSON_STEM, () -> Items.STRIPPED_CRIMSON_HYPHAE,
                () -> Items.CRIMSON_PLANKS, () -> Items.CRIMSON_STAIRS, () -> Items.CRIMSON_SLAB, () -> Items.CRIMSON_FENCE,
                () -> Items.CRIMSON_FENCE_GATE, () -> Items.CRIMSON_DOOR, () -> Items.CRIMSON_TRAPDOOR, () -> Items.CRIMSON_PRESSURE_PLATE, () -> Items.CRIMSON_BUTTON,

                () -> Items.WARPED_STEM, () -> Items.WARPED_HYPHAE, () -> Items.STRIPPED_WARPED_STEM, () -> Items.STRIPPED_WARPED_HYPHAE,
                () -> Items.WARPED_PLANKS, () -> Items.WARPED_STAIRS, () -> Items.WARPED_SLAB, () -> Items.WARPED_FENCE,
                () -> Items.WARPED_FENCE_GATE, () -> Items.WARPED_DOOR, () -> Items.WARPED_TRAPDOOR, () -> Items.WARPED_PRESSURE_PLATE, () -> Items.WARPED_BUTTON
        };

        for (Supplier<?> itemSupplier : blocks) {
            @SuppressWarnings("unchecked")
            Supplier<Item> typedSupplier = (Supplier<Item>) itemSupplier;
            entries.add(new BuildingEntry(typedSupplier, BuildingCategory.ORGANIZATION, 1, TierUnlock.UNLOCKED_BY_DEFAULT));
        }
    }

    private static Map<BuildingCategory, List<BuildingEntry>> buildCategoryIndex() {
        Map<BuildingCategory, List<BuildingEntry>> map = new EnumMap<>(BuildingCategory.class);
        for (BuildingCategory category : BuildingCategory.values()) {
            List<BuildingEntry> entries = new ArrayList<>();
            for (BuildingEntry entry : ENTRIES) {
                if (entry.category() == category) entries.add(entry);
            }
            map.put(category, Collections.unmodifiableList(entries));
        }
        return Collections.unmodifiableMap(map);
    }

    public static List<BuildingEntry> all() {
        return ENTRIES;
    }

    public static List<BuildingEntry> byCategory(BuildingCategory category) {
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    public static List<BuildingEntry> allForTier(int tier) {
        return ENTRIES.stream()
                .filter(entry -> entry.tier() == tier)
                .toList();
    }

    /**
     * @return Uses {@link TierUnlockData} if level is a ServerLevel; otherwise uses {@link TierUnlocks#clientTier()} as a fallback
     */
    public static List<BuildingEntry> allForCurrentTier(Level level) {
        int tier = TierUnlocks.clientTier();
        if (level instanceof ServerLevel serverLevel) {
            tier = TierUnlockData.get(serverLevel).tier();
        }
        return allForTier(tier);
    }

    public static List<BuildingEntry> allForTierUnlock(TierUnlock unlock) {
        return ENTRIES.stream()
                .filter(entry -> entry.unlock().equals(unlock))
                .toList();
    }

    public static Optional<BuildingEntry> byId(Identifier itemId) {
        for (BuildingEntry entry : ENTRIES) {
            if (entry.id().equals(itemId)) return Optional.of(entry);
        }
        return Optional.empty();
    }

    public static BuildingEntry getFirst() {
        return ENTRIES.getFirst();
    }

    /**
     * @param itemSupplier Throws IllegalStateException if the item isn't an instanceof BlockItem.
     *                     Only accepts a raw Item because vanilla's registry doesn't hold Item subclasses
     */
    public record BuildingEntry(Supplier<? extends Item> itemSupplier, BuildingCategory category, int tier, TierUnlock unlock) {
        public BlockItem blockItem() {
            Item item = itemSupplier.get();
            if (!(item instanceof BlockItem blockItem)) {
                throw new IllegalStateException("BuildingEntry item must be a BlockItem. Got: " + item);
            }
            return blockItem;
        }

        public Identifier id() {
            return ItemUtils.idOf(blockItem());
        }
    }
}