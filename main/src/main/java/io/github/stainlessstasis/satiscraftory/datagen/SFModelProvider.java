package io.github.stainlessstasis.satiscraftory.datagen;

import com.mojang.math.Quadrant;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.datagen.FactoryModelProvider;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SFBlocks;
import io.github.stainlessstasis.satiscraftory.registry.SFItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

public class SFModelProvider extends FactoryModelProvider {
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
    }
}
