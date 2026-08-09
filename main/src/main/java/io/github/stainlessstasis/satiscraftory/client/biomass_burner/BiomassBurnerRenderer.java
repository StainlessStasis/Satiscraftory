package io.github.stainlessstasis.satiscraftory.client.biomass_burner;

import io.github.stainlessstasis.manifold.client.LoopingParticleTracker;
import io.github.stainlessstasis.manifold.client.LoopingSoundTracker;
import io.github.stainlessstasis.manifold.client.factory_power.PoweredFactoryModel;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.manifold.util.FactoryParticles;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.manifold.client.animation.AnimationPhaseDriver;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerAnimations;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BiomassBurnerRenderer extends MultiblockRenderer<BiomassBurnerBlockEntity, BiomassBurnerRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/biomass_burner.png");
    private static final AnimationPhaseDriver ANIMATION_DRIVER = new AnimationPhaseDriver(BiomassBurnerAnimations.STARTUP, BiomassBurnerAnimations.COOLDOWN);

    private static final LoopingSoundTracker<BiomassBurnerBlockEntity> BURNING_SOUND = new LoopingSoundTracker<>();
    private static final LoopingParticleTracker<BiomassBurnerBlockEntity> LEAF_PARTICLES = new LoopingParticleTracker<>();
    private static final LoopingParticleTracker<BiomassBurnerBlockEntity> SMOKE_PARTICLES = new LoopingParticleTracker<>();
    private static final LoopingParticleTracker<BiomassBurnerBlockEntity> LARGE_SMOKE_PARTICLES = new LoopingParticleTracker<>();

    private static final long LEAF_PARTICLE_INTERVAL_MS = 250L;
    private static final double LEAF_PARTICLE_XZ_JITTER = 0.15d;

    private static final long SMOKE_PARTICLE_INTERVAL_MS = 200L;
    private static final double SMOKE_PARTICLE_XZ_JITTER = 0.1d;

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
                blockEntity
        );

        state.animationStates.copyFrom(blockEntity.animationStates);

        boolean burning = blockEntity.animationStates.loop.isStarted();

        BURNING_SOUND.playIfNeeded(
                blockEntity, burning,
                () -> new BiomassBurningSoundInstance(blockEntity, SCSounds.BIOMASS_BURNER_BURNING.value())
        );

        emitParticles(blockEntity, state.ageInTicks, burning);
    }

    private void emitParticles(BiomassBurnerBlockEntity blockEntity, float ageInTicks, boolean burning) {
        if (!(blockEntity.getLevel() instanceof Level level)) return;

        long ms = blockEntity.animationStates.loop.getTimeInMillis(ageInTicks);
        RandomSource random = level.getRandom();

        LEAF_PARTICLES.emitIfDue(blockEntity, burning, ms, LEAF_PARTICLE_INTERVAL_MS, () ->
                FactoryParticles.spawnJittered(
                        level, blockEntity.getBlockPos(), blockEntity.getLeafParticleOffset(),
                        LEAF_PARTICLE_XZ_JITTER, random, ParticleTypes.CHERRY_LEAVES
                )
        );

        SMOKE_PARTICLES.emitIfDue(blockEntity, burning, ms, SMOKE_PARTICLE_INTERVAL_MS, () ->
                FactoryParticles.spawnJittered(
                        level, blockEntity.getBlockPos(), blockEntity.getSmokeParticleOffset(),
                        SMOKE_PARTICLE_XZ_JITTER, random, ParticleTypes.LARGE_SMOKE
                )
        );

        LARGE_SMOKE_PARTICLES.emitIfDue(blockEntity, burning, ms, SMOKE_PARTICLE_INTERVAL_MS*2, () ->
                FactoryParticles.spawnJittered(
                        level, blockEntity.getBlockPos(), blockEntity.getSmokeParticleOffset(),
                        SMOKE_PARTICLE_XZ_JITTER, random, ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
                )
        );
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