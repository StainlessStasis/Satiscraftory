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
        Variant straight = new Variant(straightModelId);
        Variant straightRotated = straight.withYRot(Quadrant.R90);

        MultiVariant straightMulti = new MultiVariant(WeightedList.of(straight));
        MultiVariant straightRotatedMulti = new MultiVariant(WeightedList.of(straightRotated));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(belt)
                        .with(PropertyDispatch.initial(BeltBlock.SHAPE)
                                .select(BeltShape.NORTH_SOUTH, straightMulti)
                                .select(BeltShape.EAST_WEST, straightRotatedMulti)
                                .select(BeltShape.ASCENDING_NORTH, straightMulti)        // TODO: slope model
                                .select(BeltShape.ASCENDING_SOUTH, straightMulti)        // TODO: slope model
                                .select(BeltShape.ASCENDING_EAST, straightRotatedMulti)  // TODO: slope model
                                .select(BeltShape.ASCENDING_WEST, straightRotatedMulti)  // TODO: slope model
                                .select(BeltShape.NORTH_EAST, straightMulti)             // TODO: corner model
                                .select(BeltShape.NORTH_WEST, straightMulti)             // TODO: corner model
                                .select(BeltShape.SOUTH_EAST, straightMulti)             // TODO: corner model
                                .select(BeltShape.SOUTH_WEST, straightMulti)             // TODO: corner model
                        )
        );
    }
}
