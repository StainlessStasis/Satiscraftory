package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.datagen.FactoryModelProvider;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCBlocks;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class SCModelProvider extends FactoryModelProvider {
    public static final ModelTemplate FLAT_ITEM_2X = new ModelTemplate(
            Optional.of(Satiscraftory.id("item/flat_item_2x")),
            Optional.empty(),
            TextureSlot.LAYER0
    );

    public SCModelProvider(PackOutput output) {
        super(output, Satiscraftory.MODID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(SCBlocks.RESOURCE_NODE.get());

        Block belt_mk1 = SCBlocks.BELT_MK1.get();
        Block belt_mk2 = SCBlocks.BELT_MK2.get();
        Block belt_mk3 = SCBlocks.BELT_MK3.get();
        Item belt_mk1_item = SCItems.BELT_MK1.get();
        Item belt_mk2_item = SCItems.BELT_MK2.get();
        Item belt_mk3_item = SCItems.BELT_MK3.get();
        Identifier straight = Manifold.id("block/belt/belt_straight");
        Identifier corner = Manifold.id("block/belt/belt_curved");
        Identifier ascending = Manifold.id("block/belt/belt_ascending");

        registerBeltModels(blockModels, itemModels, belt_mk1, belt_mk1_item, straight, corner, ascending);
        registerBeltModels(blockModels, itemModels, belt_mk2, belt_mk2_item, straight, corner, ascending);
        registerBeltModels(blockModels, itemModels, belt_mk3, belt_mk3_item, straight, corner, ascending);

        itemModels.itemModelOutput.accept(SCItems.IRON_PLATE.get(), ItemModelUtils.plainModel(Satiscraftory.id("item/iron_plate")));
    }
}
