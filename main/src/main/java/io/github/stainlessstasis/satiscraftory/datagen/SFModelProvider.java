package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.datagen.FactoryModelProvider;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SFBlocks;
import io.github.stainlessstasis.satiscraftory.registry.SFItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class SFModelProvider extends FactoryModelProvider {
    public static final ModelTemplate FLAT_ITEM_2X = new ModelTemplate(
            Optional.of(Satiscraftory.id("item/flat_item_2x")),
            Optional.empty(),
            TextureSlot.LAYER0
    );

    public SFModelProvider(PackOutput output) {
        super(output, Satiscraftory.MODID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        Block belt_mk1 = SFBlocks.BELT_MK1.get();
        Block belt_mk2 = SFBlocks.BELT_MK2.get();
        Block belt_mk3 = SFBlocks.BELT_MK3.get();
        Item belt_mk1_item = SFItems.BELT_MK1.get();
        Item belt_mk2_item = SFItems.BELT_MK2.get();
        Item belt_mk3_item = SFItems.BELT_MK3.get();
        Identifier straight = Manifold.id("block/belt/belt_straight");
        Identifier corner = Manifold.id("block/belt/belt_curved");
        Identifier ascending = Manifold.id("block/belt/belt_ascending");

        registerBeltModels(blockModels, itemModels, belt_mk1, belt_mk1_item, straight, corner, ascending);
        registerBeltModels(blockModels, itemModels, belt_mk2, belt_mk2_item, straight, corner, ascending);
        registerBeltModels(blockModels, itemModels, belt_mk3, belt_mk3_item, straight, corner, ascending);

        itemModels.generateFlatItem(SFItems.IRON_PLATE.get(), ModelTemplates.FLAT_ITEM);
    }
}
