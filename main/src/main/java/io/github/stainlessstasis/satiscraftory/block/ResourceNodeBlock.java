package io.github.stainlessstasis.satiscraftory.block;

import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodePurity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        //noinspection DataFlowIssue
        if (!(level instanceof ServerLevel serverLevel) || !player.gameMode().isCreative()) return InteractionResult.PASS;
        if (!(serverLevel.getBlockEntity(pos) instanceof ResourceNodeBlockEntity nodeBE)) return InteractionResult.PASS;

        var values = ResourceNodePurity.values();
        int next = (nodeBE.getPurity().ordinal() + 1) % values.length;
        nodeBE.setPurity(values[next]);
        player.sendOverlayMessage(Component.literal("Purity set to " + values[next]));

        return InteractionResult.SUCCESS_SERVER;
    }
}