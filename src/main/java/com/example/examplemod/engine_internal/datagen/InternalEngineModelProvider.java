package com.example.examplemod.engine_internal.datagen;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.engine_internal.block.belt.BeltBlock;
import com.example.examplemod.engine_internal.block.belt.BeltShape;
import com.example.examplemod.engine_internal.registry.InternalEngineBlocks;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

public class InternalEngineModelProvider extends ModelProvider {
    public InternalEngineModelProvider(PackOutput output) {
        super(output, ExampleMod.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        Block producer = InternalEngineBlocks.PRODUCER.get();
        Block belt = InternalEngineBlocks.BELT.get();
        Block consumer = InternalEngineBlocks.CONSUMER.get();
        Block machine = InternalEngineBlocks.MACHINE.get();

        blockModels.createHorizontallyRotatedBlock(producer, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createHorizontallyRotatedBlock(machine, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createTrivialCube(consumer);

        registerBeltModels(blockModels, belt);
    }

    private void registerBeltModels(BlockModelGenerators blockModels, Block belt) {
        Identifier straightModelId = ExampleMod.id("block/belt_straight");
        Identifier curvedModelId = ExampleMod.id("block/belt_curved");

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

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(belt)
                        .with(PropertyDispatch.initial(BeltBlock.SHAPE, BeltBlock.REVERSED)
                                // straight
                                .select(BeltShape.NORTH_SOUTH, false, backwardMulti)
                                .select(BeltShape.NORTH_SOUTH, true, forwardMulti)
                                .select(BeltShape.EAST_WEST, false, backwardRotatedMulti)
                                .select(BeltShape.EAST_WEST, true, forwardRotatedMulti)

                                .select(BeltShape.ASCENDING_NORTH, false, backwardMulti)        // TODO: slope model
                                .select(BeltShape.ASCENDING_NORTH, true, forwardMulti)          // TODO: slope model
                                .select(BeltShape.ASCENDING_SOUTH, false, backwardMulti)        // TODO: slope model
                                .select(BeltShape.ASCENDING_SOUTH, true, forwardMulti)          // TODO: slope model
                                .select(BeltShape.ASCENDING_EAST, false, backwardRotatedMulti)  // TODO: slope model
                                .select(BeltShape.ASCENDING_EAST, true, forwardRotatedMulti)    // TODO: slope model
                                .select(BeltShape.ASCENDING_WEST, false, backwardRotatedMulti)  // TODO: slope model
                                .select(BeltShape.ASCENDING_WEST, true, forwardRotatedMulti)    // TODO: slope model

                                // curved
                                .select(BeltShape.NORTH_EAST, false, curveM0)
                                .select(BeltShape.NORTH_EAST, true, curveM0)
                                .select(BeltShape.NORTH_WEST, false, curveM270)
                                .select(BeltShape.NORTH_WEST, true, curveM270)
                                .select(BeltShape.SOUTH_EAST, false, curveM90)
                                .select(BeltShape.SOUTH_EAST, true, curveM90)
                                .select(BeltShape.SOUTH_WEST, false, curveM180)
                                .select(BeltShape.SOUTH_WEST, true, curveM180)
                        )
        );
    }
}
