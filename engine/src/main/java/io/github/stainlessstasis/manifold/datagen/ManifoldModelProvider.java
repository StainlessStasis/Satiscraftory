package io.github.stainlessstasis.manifold.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.NonNull;

public class ManifoldModelProvider extends ModelProvider {
    public ManifoldModelProvider(PackOutput output) {
        super(output, Manifold.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        Block producer = ManifoldBlocks.PRODUCER.get();
        Block belt_mk1 = ManifoldBlocks.BELT_MK1.get();
        Block belt_mk2 = ManifoldBlocks.BELT_MK2.get();
        Block consumer = ManifoldBlocks.CONSUMER.get();
        Block machine = ManifoldBlocks.MACHINE.get();

        blockModels.createHorizontallyRotatedBlock(producer, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createTrivialCube(consumer);

        registerMachineStates(blockModels, ManifoldBlocks.MACHINE.get());
        registerBeltModels(blockModels, belt_mk1, "");
        registerBeltModels(blockModels, belt_mk2, "_mk2");
    }

    private void registerMachineStates(BlockModelGenerators blockModels, Block machine) {
        Identifier machineModelId = Manifold.id("block/machine");

        Variant north = new Variant(machineModelId);
        Variant east  = north.withYRot(Quadrant.R90);
        Variant south = north.withYRot(Quadrant.R180);
        Variant west  = north.withYRot(Quadrant.R270);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(machine)
                        .with(PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .select(Direction.NORTH, new MultiVariant(WeightedList.of(north)))
                                .select(Direction.EAST, new MultiVariant(WeightedList.of(east)))
                                .select(Direction.SOUTH, new MultiVariant(WeightedList.of(south)))
                                .select(Direction.WEST, new MultiVariant(WeightedList.of(west)))
                        )
        );
    }

    private void registerBeltModels(BlockModelGenerators blockModels, Block belt, String appendToPath) {
        Identifier straightModelId = Manifold.id("block/belt_straight"+appendToPath);
        Identifier curvedModelId = Manifold.id("block/belt_curved"+appendToPath);
        Identifier angledModelId = Manifold.id("block/belt_ascending"+appendToPath);

        // straight
        Variant forward = new Variant(straightModelId);
        Variant forwardRotated = forward.withYRot(Quadrant.R90);
        Variant backward = forward.withYRot(Quadrant.R180);
        Variant backwardRotated = forward.withYRot(Quadrant.R270);
        MultiVariant forwardMulti = new MultiVariant(WeightedList.of(forward));
        MultiVariant forwardRotatedMulti = new MultiVariant(WeightedList.of(forwardRotated));
        MultiVariant backwardMulti = new MultiVariant(WeightedList.of(backward));
        MultiVariant backwardRotatedMulti = new MultiVariant(WeightedList.of(backwardRotated));

        // curved
        Variant curve0 = new Variant(curvedModelId);
        Variant curve90 = curve0.withYRot(Quadrant.R90);
        Variant curve180 = curve0.withYRot(Quadrant.R180);
        Variant curve270 = curve0.withYRot(Quadrant.R270);
        MultiVariant curveM0 = new MultiVariant(WeightedList.of(curve0));
        MultiVariant curveM90 = new MultiVariant(WeightedList.of(curve90));
        MultiVariant curveM180 = new MultiVariant(WeightedList.of(curve180));
        MultiVariant curveM270 = new MultiVariant(WeightedList.of(curve270));

        // angled
        Variant angle0 = new Variant(angledModelId);
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
