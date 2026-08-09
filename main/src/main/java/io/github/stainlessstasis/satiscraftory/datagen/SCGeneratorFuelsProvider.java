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
        addBiomassFuelTicks("saplings", ItemTags.SAPLINGS, 100, output);
        addBiomassFuelTicks("leaves", ItemTags.LEAVES, 20, output);
        addBiomassFuelTicks("logs", ItemTags.LOGS_THAT_BURN, 300, output);
        addBiomassFuelTicks("planks", ItemTags.PLANKS, 100, output);
        addBiomassFuelTicks("sticks", Items.STICK, 20, output);
        addBiomassFuelTicks("bamboo", Items.BAMBOO, 20, output);
        addBiomassFuelTicks("scaffolding", Items.SCAFFOLDING, 400, output);
        addBiomassFuelTicks("wool", ItemTags.WOOL, 100, output);
        addBiomassFuel("wheat", Items.WHEAT, 2f, output);
        addBiomassFuel("hay_bale", Items.HAY_BLOCK, 20f, output);
        addBiomassFuel("carrot", Items.CARROT, 2f, output);
        addBiomassFuel("potato", Items.POTATO, 2f, output);
        addBiomassFuel("beetroot", Items.BEETROOT, 2f, output);
        addBiomassFuel("sugar_cane", Items.SUGAR_CANE, 2f, output);
        addBiomassFuel("cactus", Items.CACTUS, 2f, output);
        addBiomassFuel("red_mushroom", Items.RED_MUSHROOM, 2f, output);
        addBiomassFuel("brown_mushroom", Items.BROWN_MUSHROOM, 2f, output);
        addBiomassFuel("moss_block", Items.MOSS_BLOCK, 2f, output);
        addBiomassFuel("kelp", Items.KELP, 1f, output);
        addBiomassFuelTicks("dried_kelp_block", Items.DRIED_KELP_BLOCK, 4000, output);
        addBiomassFuel("seagrass", Items.SEAGRASS, 1f, output);
        addBiomassFuel("vine", Items.VINE, 1f, output);
        addBiomassFuel("lily_pad", Items.LILY_PAD, 1f, output);
        addBiomassFuel("sweet_berries", Items.SWEET_BERRIES, 1f, output);
        addBiomassFuel("glow_berries", Items.GLOW_BERRIES, 1f, output);
        addBiomassFuel("cocoa_beans", Items.COCOA_BEANS, 1f, output);
        addBiomassFuel("melon_slice", Items.MELON_SLICE, 1f, output);
        addBiomassFuel("pumpkin", Items.PUMPKIN, 6f, output);
        addBiomassFuel("melon", Items.MELON, 6f, output);
    }

    private void addBiomassFuel(String id, Item item, float burnSeconds, FuelOutput output) {
        addBiomassFuelTicks(id, item, Math.round(burnSeconds * 20), output);
    }

    private void addBiomassFuelTicks(String id, Item item, int ticks, FuelOutput output) {
        output.accept(
                Satiscraftory.id("biomass_" + id),
                GeneratorFuel.Data.ofItem(SCGeneratorTypes.BIOMASS, item, ticks)
        );
    }

    private void addBiomassFuelTicks(String id, TagKey<Item> tag, int ticks, FuelOutput output) {
        output.accept(
                Satiscraftory.id("biomass_" + id),
                GeneratorFuel.Data.ofTag(SCGeneratorTypes.BIOMASS, tag, ticks)
        );
    }
}
