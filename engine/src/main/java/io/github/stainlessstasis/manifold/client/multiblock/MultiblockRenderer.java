package io.github.stainlessstasis.manifold.client.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class MultiblockRenderer<T extends BlockEntity, S extends MultiblockRenderState> implements BlockEntityRenderer<T, S> {
    protected abstract MultiblockShape shape();
    protected abstract void submitModel(@NonNull S renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector);

    @Override
    public void extractRenderState(
            @NonNull T blockEntity, @NonNull S renderState, float partialTick,
            @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public void submit(
            @NonNull S renderState, @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.facing.toYRot()));
        poseStack.translate(-0.5, 0, -0.5);

        submitModel(renderState, poseStack, collector);

        poseStack.popPose();
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(@NonNull T blockEntity) {
        BlockPos origin = blockEntity.getBlockPos();
        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        AABB box = null;
        for (BlockPos pos : shape().absoluteAllPositions(origin, facing)) {
            AABB cell = new AABB(pos);
            box = (box == null) ? cell : box.minmax(cell);
        }
        return box != null ? box : BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }
}