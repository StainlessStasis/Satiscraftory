package io.github.stainlessstasis.manifold.block_entity.factory_component;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Predicate;

public class ManifoldBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {
    private final Predicate<Block> validator;

    public ManifoldBlockEntityType(BlockEntitySupplier<T> factory, Predicate<Block> validator) {
        super(factory, Set.of());
        this.validator = validator;
    }

    @Override
    public boolean isValid(BlockState state) {
        return validator.test(state.getBlock());
    }
}
