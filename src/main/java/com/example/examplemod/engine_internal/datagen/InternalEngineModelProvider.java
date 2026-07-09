package com.example.examplemod.engine_internal.datagen;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.engine_internal.registry.InternalEngineBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
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
        blockModels.createHorizontallyRotatedBlock(belt, TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createHorizontallyRotatedBlock(machine, TexturedModel.ORIENTABLE_ONLY_TOP);

        blockModels.createTrivialCube(consumer);
    }
}
