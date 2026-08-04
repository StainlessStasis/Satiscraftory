package io.github.stainlessstasis.manifold.client.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.stainlessstasis.manifold.client.factory_power.PoweredFactoryModel;
import io.github.stainlessstasis.manifold.factory_power.PowerConsumingFactoryBlockEntity;
import io.github.stainlessstasis.manifold.factory_power.PowerIndicatorState;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class MultiblockRenderer<T extends BlockEntity, S extends MultiblockRenderState> implements BlockEntityRenderer<T, S> {
    protected abstract MultiblockShape shape();
    public abstract Identifier getTexture();
    public abstract Model<S> getModel();

    protected MultiblockRenderer(BlockEntityType<T> blockEntityType) {
        MultiblockPreviewRegistry.register(blockEntityType, this);
    }


    @Override
    public void extractRenderState(
            @NonNull T blockEntity, @NonNull S renderState, float partialTick,
            @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (blockEntity.hasLevel()) {
            @SuppressWarnings("DataFlowIssue") // null checked above
            long gameTime = blockEntity.getLevel().getGameTime();
            renderState.gameTime = gameTime;
            renderState.ageInTicks = gameTime + partialTick;
        }

        if (blockEntity instanceof PowerConsumingFactoryBlockEntity<?> powerConsumingBlockEntity) {
            renderState.powerIndicatorState = powerConsumingBlockEntity.getPowerIndicatorState();
        }
    }

    private void applyTransform(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        poseStack.scale(-1, -1, 1);
        poseStack.translate(0, EntityModel.MODEL_Y_OFFSET, -0.125);
    }

    @Override
    public void submit(
            @NonNull S renderState, @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        poseStack.pushPose();
        applyTransform(poseStack, renderState.facing);
        collector.submitModel(getModel(), renderState, poseStack, getTexture(), renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0, null);
        submitPowerIndicator(renderState, poseStack);
        poseStack.popPose();
    }

    private void submitPowerIndicator(S renderState, PoseStack poseStack) {
        if (!(getModel() instanceof PoweredFactoryModel<S> model)) {
            return;
        }

        ModelPart indicatorPart = model.getPowerIndicatorPart();
        if (indicatorPart == null) return;

        int tintColor = renderState.powerIndicatorState.color.getRGB();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.entityCutout(getTexture()));

        poseStack.pushPose();
        ModelPart parent = model.getPowerIndicatorParent();
        if (parent != null) {
            parent.translateAndRotate(poseStack);
        }
        indicatorPart.visible = true;
        indicatorPart.render(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, tintColor);
        indicatorPart.visible = false;
        poseStack.popPose();

        bufferSource.endBatch(RenderTypes.entityCutout(getTexture()));
    }

    public void submitPreview(PoseStack poseStack, SubmitNodeCollector collector, Direction facing, int lightCoords, int tintColor) {
        S renderState = createRenderState();
        renderState.facing = facing;
        renderState.lightCoords = lightCoords;

        poseStack.pushPose();
        applyTransform(poseStack, facing);
        collector.submitModel(
                getModel(), renderState, poseStack,
                RenderTypes.entityTranslucent(getTexture()),
                lightCoords, OverlayTexture.NO_OVERLAY,
                tintColor, null, 0, null
        );
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

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}