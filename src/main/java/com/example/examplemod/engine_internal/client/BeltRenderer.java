package com.example.examplemod.engine_internal.client;

import com.example.examplemod.engine_internal.PayloadItems;
import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
    private final ItemModelResolver itemModelResolver;

    public BeltRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NonNull BeltRenderState createRenderState() {
        return new BeltRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull BeltBlockEntity blockEntity, @NonNull BeltRenderState renderState, float partialTick,
            @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);

        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        renderState.items.clear();

        for (var itemSnapshot : blockEntity.getRenderItems()) {
            BeltRenderState.BeltItemRenderData itemRenderData = new BeltRenderState.BeltItemRenderData(itemSnapshot.position());
            ItemStack itemStack = PayloadItems.toItemStack(itemSnapshot.typeId(), 1);
            if (itemStack == null) continue;

            itemModelResolver.updateForTopItem(itemRenderData.itemStackRenderState, itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
            renderState.items.add(itemRenderData);
        }
    }

    @Override
    public void submit(
            BeltRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        for (BeltRenderState.BeltItemRenderData itemRenderData : renderState.items) {
            double travelOffset = itemRenderData.position - 0.5;
            double offsetX = renderState.facing.getStepX() * travelOffset;
            double offsetZ = renderState.facing.getStepZ() * travelOffset;

            poseStack.pushPose();
            poseStack.translate(0.5 + offsetX, 1, 0.5 + offsetZ);
            poseStack.scale(0.3f, 0.3f, 0.3f);

            itemRenderData.itemStackRenderState.submit(poseStack, collector, renderState.lightCoords, 0, 0);

            poseStack.popPose();
        }
    }
}