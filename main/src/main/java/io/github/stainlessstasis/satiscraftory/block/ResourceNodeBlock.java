package io.github.stainlessstasis.satiscraftory.block;

import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ResourceNodeBlock extends Block implements EntityBlock {
    private final Identifier resourceItemId;

    public ResourceNodeBlock(Properties properties, Identifier resourceItemId) {
        super(properties);
        this.resourceItemId = resourceItemId;
    }

    public Identifier getResourceType() {
        return resourceItemId;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        ResourceNodeBlockEntity blockEntity = new ResourceNodeBlockEntity(SCBlockEntities.RESOURCE_NODE_MARKER.get(), pos, state);
        blockEntity.setResourceType(resourceItemId);
        return blockEntity;
    }
}