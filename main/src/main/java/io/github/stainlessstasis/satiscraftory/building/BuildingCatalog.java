package io.github.stainlessstasis.satiscraftory.building;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlock;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class BuildingCatalog {
    private static final List<BuildingEntry> ENTRIES = List.of(
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
            new BuildingEntry(ManifoldItems.CONSUMER, BuildingCategory.LOGISTICS, 1, TierUnlock.UNLOCKED_BY_DEFAULT)
    );

    private static final Map<BuildingCategory, List<BuildingEntry>> BY_CATEGORY = buildCategoryIndex();

    private BuildingCatalog() {}

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

    public static Optional<BuildingEntry> byId(Identifier itemId) {
        for (BuildingEntry entry : ENTRIES) {
            if (entry.id().equals(itemId)) return Optional.of(entry);
        }
        return Optional.empty();
    }

    public static BuildingEntry first() {
        return ENTRIES.getFirst();
    }

    public record BuildingEntry(Supplier<? extends BlockItem> item, BuildingCategory category, int tier, TierUnlock unlock) {
        public BlockItem blockItem() {
            return item.get();
        }

        public Identifier id() {
            return ItemUtils.idOf(blockItem());
        }
    }
}