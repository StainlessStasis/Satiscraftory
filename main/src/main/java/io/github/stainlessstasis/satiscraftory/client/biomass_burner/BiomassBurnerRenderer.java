package io.github.stainlessstasis.satiscraftory.client.biomass_burner;

import io.github.stainlessstasis.manifold.client.factory_power.PoweredFactoryModel;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.manifold.animation.AnimationPhaseTransition;
import io.github.stainlessstasis.manifold.client.animation.AnimationPhaseDriver;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerAnimations;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BiomassBurnerRenderer extends MultiblockRenderer<BiomassBurnerBlockEntity, BiomassBurnerRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/biomass_burner.png");
    private static final AnimationPhaseDriver ANIMATION_DRIVER = new AnimationPhaseDriver(BiomassBurnerAnimations.STARTUP, BiomassBurnerAnimations.COOLDOWN);
    private final BiomassBurnerModel model;

    public BiomassBurnerRenderer(BlockEntityRendererProvider.Context context) {
        super(SCBlockEntities.BIOMASS_BURNER.get());
        this.model = new BiomassBurnerModel(context.bakeLayer(BiomassBurnerModel.LAYER_LOCATION));
    }

    @Override
    public void extractRenderState(
            @NonNull BiomassBurnerBlockEntity blockEntity, @NonNull BiomassBurnerRenderState state, float partialTick,
            @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPosition, crumblingOverlay);

        blockEntity.animationPhase = ANIMATION_DRIVER.tick(
                blockEntity.animationStates, blockEntity.animationPhase,
                state.gameTime, state.ageInTicks, blockEntity.isEffectivelyBurning(),
                AnimationPhaseTransition.NO_OP
        );

        state.animationStates.copyFrom(blockEntity.animationStates);
    }

    @Override
    protected MultiblockShape shape() {
        return BiomassBurnerBlock.MULTIBLOCK_SHAPE;
    }

    @Override
    public Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public PoweredFactoryModel<BiomassBurnerRenderState> getModel() {
        return model;
    }

    @Override
    public @NonNull BiomassBurnerRenderState createRenderState() {
        return new BiomassBurnerRenderState();
    }
}