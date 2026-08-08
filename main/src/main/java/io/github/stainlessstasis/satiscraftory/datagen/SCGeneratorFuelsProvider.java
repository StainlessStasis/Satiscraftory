package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.datagen.ManifoldGeneratorFuelProvider;
import io.github.stainlessstasis.manifold.recipe.GeneratorFuel;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCGeneratorTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SCGeneratorFuelsProvider extends ManifoldGeneratorFuelProvider {
    public SCGeneratorFuelsProvider(PackOutput output) {
        super(output);
    }

    // TODO: refactor fuels to use tags and add saplings/logs
    @Override
    protected void addFuels(FuelOutput output) {
        addBiomassFuel("wheat", Items.WHEAT, 4f, output);
        addBiomassFuel("hay_bale", Items.HAY_BLOCK, 30f, output);
    }

    private void addBiomassFuel(String id, Item item, float burnSeconds, FuelOutput output) {
        output.accept(
                Satiscraftory.id("biomass_" + id),
                GeneratorFuel.Data.of(SCGeneratorTypes.BIOMASS, item, Math.round(burnSeconds * 20))
        );
    }
}
