package io.github.stainlessstasis.manifold.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import com.mojang.math.Quadrant;
import io.github.stainlessstasis.manifold.registry.ManifoldItems;
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

public class ManifoldModelProvider extends FactoryModelProvider {
    public ManifoldModelProvider(PackOutput output) {
        super(output, Manifold.MODID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        Block producer = ManifoldBlocks.PRODUCER.get();
        Block consumer = ManifoldBlocks.CONSUMER.get();
        Block machine = ManifoldBlocks.MACHINE.get();
        Block container = ManifoldBlocks.CONTAINER.get();

        registerHorizontallyRotable(blockModels, producer, "block/producer", false);
        registerHorizontallyRotable(blockModels, machine, "block/machine", false);
        registerHorizontallyRotable(blockModels, container, "block/container", false);
        registerHorizontallyRotable(blockModels, consumer, "block/consumer", false);
    }
}
