package io.github.stainlessstasis.satiscraftory.client.miner;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block.MinerBlock;
import io.github.stainlessstasis.satiscraftory.block_entity.MinerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class MinerRenderer extends MultiblockRenderer<MinerBlockEntity, MinerRenderState> {
    private static final Identifier TEXTURE = Satiscraftory.id("textures/factory/miner.png");

    private final MinerModel model;

    public MinerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MinerModel(context.bakeLayer(MinerModel.LAYER_LOCATION));
    }

    @Override
    public @NonNull MinerRenderState createRenderState() {
        return new MinerRenderState();
    }

    @Override
    protected MultiblockShape shape() {
        return MinerBlock.MULTIBLOCK_SHAPE;
    }

    @Override
    protected void submitModel(@NonNull MinerRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector) {
        RenderType renderType = RenderTypes.entityCutout(TEXTURE);
        collector.submitModelPart(model.root(), poseStack, renderType, renderState.lightCoords, OverlayTexture.NO_OVERLAY, null);
    }
}