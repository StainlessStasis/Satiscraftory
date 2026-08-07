package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.datagen.FactoryModelProvider;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCBlocks;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
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

    public static final TextureSlot PARTICLE_SLOT = TextureSlot.create("particle");
    public static final ModelTemplate PARTICLE_ONLY = new ModelTemplate(
            Optional.of(Satiscraftory.id("block/particle_only")),
            Optional.empty(),
            PARTICLE_SLOT
    );

    public SCModelProvider(PackOutput output) {
        super(output, Satiscraftory.MODID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        for (var itemHolder : SCItems.getFactoryItems()) {
            Item item = itemHolder.get();
            Identifier itemId = itemHolder.getKey().identifier();
            itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(Satiscraftory.id("item/" + itemId.getPath())));
        }

        for (var type : SCResourceNodes.TYPES) {
            blockModels.createTrivialCube(type.getNodeBlock().get());
        }

        registerFactoryBuilding("miner", SCBlocks.MINER_MK1.get(), SCItems.MINER_MK1.get(), blockModels, itemModels);
        registerFactoryBuilding("power_pole", SCBlocks.POWER_POLE_MK1.get(), SCItems.POWER_POLE_MK1.get(), blockModels, itemModels);
        registerFactoryBuilding("biomass_burner", SCBlocks.BIOMASS_BURNER.get(), SCItems.BIOMASS_BURNER.get(), blockModels, itemModels);

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
    }

    private void registerFactoryBuilding(String id, Block block, Item item, @NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        Identifier particle = PARTICLE_ONLY.create(
                block,
                new TextureMapping().put(PARTICLE_SLOT, new Material(Satiscraftory.id("block/"+id))),
                blockModels.modelOutput
        );
        registerHorizontallyRotable(blockModels, block, particle, false);
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }
}
