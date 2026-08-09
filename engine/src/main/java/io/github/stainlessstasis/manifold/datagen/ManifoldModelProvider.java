package io.github.stainlessstasis.manifold.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

public class ManifoldModelProvider extends FactoryModelProvider {
    public ManifoldModelProvider(PackOutput output) {
        super(output, Manifold.MODID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        registerHorizontallyRotable(blockModels, ManifoldBlocks.PRODUCER.get(), "block/producer", false);
        registerHorizontallyRotable(blockModels, ManifoldBlocks.POWER_PRODUCER.get(), "block/producer", false);
        registerHorizontallyRotable(blockModels, ManifoldBlocks.MACHINE.get(), "block/machine", false);
        registerHorizontallyRotable(blockModels, ManifoldBlocks.CONTAINER.get(), "block/container", false);
        registerHorizontallyRotable(blockModels, ManifoldBlocks.CONSUMER.get(), "block/consumer", false);

        registerHorizontallyRotable(blockModels, ManifoldBlocks.SPLITTER.get(), "block/splitter_merger", false);
        registerHorizontallyRotable(blockModels, ManifoldBlocks.MERGER.get(), "block/splitter_merger", false);
        // need to manually override the item models, otherwise they default to "splitter" and "merger" which is wrong
        Identifier splitter_mergerModel = Manifold.id("block/splitter_merger");
        itemModels.itemModelOutput.accept(ManifoldItems.SPLITTER.get(), ItemModelUtils.plainModel(splitter_mergerModel));
        itemModels.itemModelOutput.accept(ManifoldItems.MERGER.get(), ItemModelUtils.plainModel(splitter_mergerModel));

        itemModels.generateFlatItem(ManifoldItems.CABLE_CUTTER.get(), ModelTemplates.FLAT_ITEM);

        // no-op
        blockModels.createAirLikeBlock(ManifoldBlocks.MULTIBLOCK_FILLER.get(), Items.BARRIER);
        itemModels.generateFlatItem(ManifoldItems.POWER_LINK.get(), Items.STICK, ModelTemplates.FLAT_ITEM);
    }
}
