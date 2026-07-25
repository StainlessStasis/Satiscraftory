package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerAnimations;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MinerRenderer extends MultiblockRenderer<MinerBlockEntity, MinerRenderState> {
    private static final Identifier TEXTURE = Satiscraftory.id("textures/factory/miner.png");

    private final MinerModel model;

    public MinerRenderer(BlockEntityRendererProvider.Context context) {
        super(SCBlockEntities.MINER.get());
        this.model = new MinerModel(context.bakeLayer(MinerModel.LAYER_LOCATION));
    }

    @Override
    public void extractRenderState(
            @NonNull MinerBlockEntity blockEntity, @NonNull MinerRenderState state, float partialTick,
            @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPosition, crumblingOverlay);

        if (!blockEntity.startupAnimationState.isStarted() && !blockEntity.spinAnimationState.isStarted()) {
            blockEntity.startupAnimationState.start((int) state.gameTime);
        }

        boolean startupEnded = blockEntity.startupAnimationState.getTimeInMillis(state.ageInTicks) >= MinerAnimations.STARTUP.lengthInSeconds() * 1000L;
        if (blockEntity.startupAnimationState.isStarted() && startupEnded) {
            blockEntity.startupAnimationState.stop();
            blockEntity.spinAnimationState.start((int) state.gameTime);
        }

        state.startupAnimationState.copyFrom(blockEntity.startupAnimationState);
        state.spinAnimationState.copyFrom(blockEntity.spinAnimationState);
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
    public Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public Model<MinerRenderState> getModel() {
        return model;
    }
}