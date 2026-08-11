package io.github.stainlessstasis.satiscraftory.datagen.building_cost;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCost;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.NonNull;

// TODO: replace placeholders with real implementations (also concrete doesnt exist yet, and SF uses concrete for a lot of buildings...)
public class SCBuildingCostsProvider extends BuildingCostProvider {
    public SCBuildingCostsProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addCosts(CostOutput output) {
        addCost(output, SCItems.MINER_MK1, BuildingCost.Data.builder(SCItems.MINER_MK1.get())
                .input(SCItems.IRON_PLATE.get(), 10) // TODO: add concrete
                .build());
        addCost(output, SCItems.BELT_MK1, BuildingCost.Data.builder(SCItems.BELT_MK1.get())
                .input(SCItems.IRON_PLATE.get(), 1)
                .build());
        addCost(output, SCItems.BELT_MK2, BuildingCost.Data.builder(SCItems.BELT_MK2.get())
                .input(SCItems.IRON_PLATE.get(), 6) // TODO: replace with reinforced iron plate
                .input(SCItems.SCREWS.get(), 12)
                .build());
        addCost(output, SCItems.BELT_MK3, BuildingCost.Data.builder(SCItems.BELT_MK3.get())
                .input(Items.BEDROCK, 999) // TODO: replace with steel later, unobtainable for now
                .build());
        addCost(output, SCItems.POWER_POLE_MK1, BuildingCost.Data.builder(SCItems.POWER_POLE_MK1.get())
                .input(SCItems.IRON_ROD.get(), 1) // TODO: add concrete
                .input(SCItems.WIRE.get(), 3)
                .build());
        addCost(output, SCItems.BIOMASS_BURNER, BuildingCost.Data.builder(SCItems.BIOMASS_BURNER.get())
                .input(SCItems.IRON_PLATE.get(), 15)
                .input(SCItems.IRON_ROD.get(), 15)
                .input(SCItems.WIRE.get(), 25)
                .build());
        addCost(output, ManifoldItems.SPLITTER, BuildingCost.Data.builder(ManifoldItems.SPLITTER.get())
                .input(SCItems.IRON_PLATE.get(), 2)
                .input(SCItems.CABLE.get(), 2)
                .build());
        addCost(output, ManifoldItems.MERGER, BuildingCost.Data.builder(ManifoldItems.MERGER.get())
                .input(SCItems.IRON_PLATE.get(), 2)
                .input(SCItems.IRON_ROD.get(), 2)
                .build());
        addCost(output, ManifoldItems.MACHINE, BuildingCost.Data.builder(ManifoldItems.MACHINE.get())
                .input(SCItems.IRON_PLATE.get(), 8) // TODO: separate into smelter/constructor in 0.7.0 with their actual recipes
                .input(SCItems.IRON_ROD.get(), 4)
                .input(SCItems.WIRE.get(), 8)
                .build());
        addCost(output, ManifoldItems.CONTAINER, BuildingCost.Data.builder(ManifoldItems.CONTAINER.get())
                .input(SCItems.IRON_PLATE.get(), 10)
                .input(SCItems.IRON_ROD.get(), 10)
                .build());
        addCost(output, ManifoldItems.CONTAINER, BuildingCost.Data.builder(ManifoldItems.CONTAINER.get())
                .input(SCItems.IRON_PLATE.get(), 10)
                .input(SCItems.IRON_ROD.get(), 10)
                .build());
        addCost(output, ManifoldItems.CONSUMER, BuildingCost.Data.builder(ManifoldItems.CONSUMER.get()) // TODO: replace with awesome sink eventually
                .input(SCItems.IRON_PLATE.get(), 15)
                .input(SCItems.IRON_ROD.get(), 10)
                .input(SCItems.WIRE.get(), 15)
                .build());
    }

    private void addCost(CostOutput output, ItemLike buildingItem, BuildingCost.Data data) {
        String name = BuiltInRegistries.ITEM.getKey(buildingItem.asItem()).getPath();
        output.accept(Satiscraftory.id(name), data);
    }

    @Override
    public @NonNull String getName() {
        return "Satiscraftory Building Costs";
    }
}