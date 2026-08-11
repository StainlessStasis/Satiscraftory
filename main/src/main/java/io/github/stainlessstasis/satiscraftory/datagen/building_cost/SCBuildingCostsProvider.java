package io.github.stainlessstasis.satiscraftory.datagen.building_cost;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCost;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;

// TODO: replace placeholders with real implementations (also concrete doesnt exist yet, and SF uses concrete for a lot of buildings...)
public class SCBuildingCostsProvider extends BuildingCostProvider {
    public SCBuildingCostsProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addCosts(CostOutput output) {
        addCost(output, ManifoldItems.MERGER, BuildingCost.Data.builder(ManifoldItems.MERGER.get())
                .input(SCItems.IRON_PLATE.get(), 2)
                .input(SCItems.IRON_ROD.get(), 2)
                .build());

        addPlaceholder(output, SCItems.MINER_MK1);
        addPlaceholder(output, SCItems.BELT_MK1);
        addPlaceholder(output, SCItems.BELT_MK2);
        addPlaceholder(output, SCItems.BELT_MK3);
        addPlaceholder(output, SCItems.POWER_POLE_MK1);
        addPlaceholder(output, SCItems.BIOMASS_BURNER);
        addPlaceholder(output, ManifoldItems.SPLITTER);
        addPlaceholder(output, ManifoldItems.MACHINE);
        addPlaceholder(output, ManifoldItems.CONTAINER);
        addPlaceholder(output, ManifoldItems.CONSUMER);
        addPlaceholder(output, ManifoldItems.POWER_PRODUCER);
    }

    private void addPlaceholder(CostOutput output, ItemLike buildingItem) {
        addCost(output, buildingItem, BuildingCost.Data.builder(buildingItem)
                .input(SCItems.IRON_PLATE.get(), 1)
                .build());
    }

    private void addCost(CostOutput output, ItemLike buildingItem, BuildingCost.Data data) {
        String name = BuiltInRegistries.ITEM.getKey(buildingItem.asItem()).getPath();
        output.accept(Satiscraftory.id(name), data);
    }
}