package io.github.stainlessstasis.manifold.datagen;

import com.mojang.math.Quadrant;
import io.github.stainlessstasis.manifold.block.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.factory_component.belt.BeltShape;
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

public abstract class FactoryModelProvider extends ModelProvider {
    public FactoryModelProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    public void registerHorizontallyRotable(BlockModelGenerators blockModels, Block block, String path, boolean reversed) {
        registerHorizontallyRotable(blockModels, block, Identifier.fromNamespaceAndPath(modId, path), reversed);
    }

    public void registerHorizontallyRotable(BlockModelGenerators blockModels, Block block, Identifier id, boolean reversed) {
        Variant north = new Variant(id);
        Variant east  = north.withYRot(Quadrant.R90);
        Variant south = north.withYRot(Quadrant.R180);
        Variant west  = north.withYRot(Quadrant.R270);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block)
                        .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .select(Direction.NORTH, new MultiVariant(WeightedList.of( !reversed ? north : south)))
                                .select(Direction.EAST, new MultiVariant(WeightedList.of(  !reversed ? east : west)))
                                .select(Direction.SOUTH, new MultiVariant(WeightedList.of( !reversed ? south : north)))
                                .select(Direction.WEST, new MultiVariant(WeightedList.of(  !reversed ? west : east)))
                        )
        );
    }

    public void registerBeltModels(
            BlockModelGenerators blockModels, ItemModelGenerators itemModels,
            Block belt, Item beltItem, Identifier straightModelId, Identifier cornerModelId, Identifier ascendingModelId
    ) {
        itemModels.itemModelOutput.accept(beltItem, ItemModelUtils.plainModel(straightModelId.withSuffix("_item")));

        // straight
        Variant forward = new Variant(straightModelId);
        Variant forwardRotated = forward.withYRot(Quadrant.R90);
        Variant backward = forward.withYRot(Quadrant.R180);
        Variant backwardRotated = forward.withYRot(Quadrant.R270);
        MultiVariant forwardMulti = new MultiVariant(WeightedList.of(forward));
        MultiVariant forwardRotatedMulti = new MultiVariant(WeightedList.of(forwardRotated));
        MultiVariant backwardMulti = new MultiVariant(WeightedList.of(backward));
        MultiVariant backwardRotatedMulti = new MultiVariant(WeightedList.of(backwardRotated));

        // corner
        Variant curve0 = new Variant(cornerModelId);
        Variant curve90 = curve0.withYRot(Quadrant.R90);
        Variant curve180 = curve0.withYRot(Quadrant.R180);
        Variant curve270 = curve0.withYRot(Quadrant.R270);
        MultiVariant curveM0 = new MultiVariant(WeightedList.of(curve0));
        MultiVariant curveM90 = new MultiVariant(WeightedList.of(curve90));
        MultiVariant curveM180 = new MultiVariant(WeightedList.of(curve180));
        MultiVariant curveM270 = new MultiVariant(WeightedList.of(curve270));

        // ascending
        Variant angle0 = new Variant(ascendingModelId);
        Variant angle90 = angle0.withYRot(Quadrant.R90);
        Variant angle180 = angle0.withYRot(Quadrant.R180);
        Variant angle270 = angle0.withYRot(Quadrant.R270);
        MultiVariant angleM0 = new MultiVariant(WeightedList.of(angle0));
        MultiVariant angleM90 = new MultiVariant(WeightedList.of(angle90));
        MultiVariant angleM180 = new MultiVariant(WeightedList.of(angle180));
        MultiVariant angleM270 = new MultiVariant(WeightedList.of(angle270));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(belt)
                        .with(PropertyDispatch.initial(BeltBlock.SHAPE, BeltBlock.REVERSED)
                                // straight
                                .select(BeltShape.NORTH_SOUTH, false, backwardMulti)
                                .select(BeltShape.NORTH_SOUTH, true, forwardMulti)
                                .select(BeltShape.EAST_WEST, false, backwardRotatedMulti)
                                .select(BeltShape.EAST_WEST, true, forwardRotatedMulti)

                                // curved
                                .select(BeltShape.NORTH_EAST, false, curveM0)
                                .select(BeltShape.NORTH_EAST, true, curveM0)
                                .select(BeltShape.NORTH_WEST, false, curveM270)
                                .select(BeltShape.NORTH_WEST, true, curveM270)
                                .select(BeltShape.SOUTH_EAST, false, curveM90)
                                .select(BeltShape.SOUTH_EAST, true, curveM90)
                                .select(BeltShape.SOUTH_WEST, false, curveM180)
                                .select(BeltShape.SOUTH_WEST, true, curveM180)

                                // angled
                                .select(BeltShape.ASCENDING_NORTH, false, angleM180)
                                .select(BeltShape.ASCENDING_NORTH, true, angleM180)
                                .select(BeltShape.ASCENDING_SOUTH, false, angleM0)
                                .select(BeltShape.ASCENDING_SOUTH, true, angleM0)
                                .select(BeltShape.ASCENDING_EAST, false, angleM270)
                                .select(BeltShape.ASCENDING_EAST, true, angleM270)
                                .select(BeltShape.ASCENDING_WEST, false, angleM90)
                                .select(BeltShape.ASCENDING_WEST, true, angleM90)
                        )
        );
    }
}
