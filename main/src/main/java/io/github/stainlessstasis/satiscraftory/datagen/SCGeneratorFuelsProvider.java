package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.datagen.ManifoldGeneratorFuelProvider;
import io.github.stainlessstasis.manifold.recipe.GeneratorFuel;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCGeneratorTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class SCGeneratorFuelsProvider extends ManifoldGeneratorFuelProvider {
    public SCGeneratorFuelsProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addFuels(FuelOutput output) {
        addBiomassFuel("saplings", ItemTags.SAPLINGS, 2f, output);
        addBiomassFuel("logs", ItemTags.LOGS_THAT_BURN, 10f, output);
        addBiomassFuel("wheat", Items.WHEAT, 4f, output);
        addBiomassFuel("hay_bale", Items.HAY_BLOCK, 30f, output);
    }

    private void addBiomassFuel(String id, Item item, float burnSeconds, FuelOutput output) {
        output.accept(
                Satiscraftory.id("biomass_" + id),
                GeneratorFuel.Data.ofItem(SCGeneratorTypes.BIOMASS, item, Math.round(burnSeconds * 20))
        );
    }

    private void addBiomassFuel(String id, TagKey<Item> tag, float burnSeconds, FuelOutput output) {
        output.accept(
                Satiscraftory.id("biomass_" + id),
                GeneratorFuel.Data.ofTag(SCGeneratorTypes.BIOMASS, tag, Math.round(burnSeconds * 20))
        );
    }
}
