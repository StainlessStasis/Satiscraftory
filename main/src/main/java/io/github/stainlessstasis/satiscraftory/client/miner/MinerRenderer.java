package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.util.LoopingSoundTracker;
import io.github.stainlessstasis.manifold.client.animation.AnimationPhaseDriver;
import io.github.stainlessstasis.manifold.client.model.PoweredFactoryModel;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerAnimations;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MinerRenderer extends MultiblockRenderer<MinerBlockEntity, MinerRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/miner.png");
    public static final Vec3 MODEL_OFFSET = new Vec3(0, EntityModel.MODEL_Y_OFFSET, -0.125);

    private static final AnimationPhaseDriver ANIMATION_DRIVER = new AnimationPhaseDriver(MinerAnimations.STARTUP_ROTATION, MinerAnimations.COOLDOWN);
    private static final LoopingSoundTracker<MinerBlockEntity> DRILL_SOUND = new LoopingSoundTracker<>();

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

        boolean shouldRun = blockEntity.isPowered() && !blockEntity.isBufferFull();
        blockEntity.animationPhase = ANIMATION_DRIVER.tick(
                blockEntity.animationStates, blockEntity.animationPhase,
                state.gameTime, state.ageInTicks, shouldRun, blockEntity
        );
        state.animationStates.copyFrom(blockEntity.animationStates);

        state.resourceNodeId = blockEntity.getResourceNodeId() != null
                ? blockEntity.getResourceNodeId()
                : SCResourceNodes.IRON.getNodeId();

        DRILL_SOUND.playIfNeeded(
                blockEntity,
                blockEntity.animationStates.loop.isStarted(),
                () -> new MinerDrillSoundInstance(blockEntity, SCSounds.MINER_DRILLING.value())
        );
        spawnDrillParticles(blockEntity, state.ageInTicks, state.resourceNodeId);
    }

    private void spawnDrillParticles(MinerBlockEntity blockEntity, float ageInTicks, Identifier resourceNodeId) {
        if (!(blockEntity.getLevel() instanceof Level level)) return;
        if (!blockEntity.animationStates.loop.isStarted()) return;

        long ms = blockEntity.animationStates.loop.getTimeInMillis(ageInTicks);
        long deltaTime = ms - blockEntity.getLastParticleTime();
        if (deltaTime < MinerBlockEntity.PARTICLE_INTERVAL_MS) return;

        blockEntity.setLastParticleTime(ms);

        ParticleOptions particle = SCResourceNodes.particleFor(resourceNodeId);
        BlockPos pos = blockEntity.getBlockPos();
        Vec3 offset = blockEntity.getParticleOffset();
        RandomSource random = level.getRandom();

        double x = pos.getX() + 0.5 + offset.x + (random.nextDouble() - 0.5) * 2 * MinerBlockEntity.PARTICLE_JITTER;
        double y = pos.getY() + 0.25 + offset.y + (random.nextDouble() - 0.5) * 2 * MinerBlockEntity.PARTICLE_JITTER;
        double z = pos.getZ() + 0.5 + offset.z + (random.nextDouble() - 0.5) * 2 * MinerBlockEntity.PARTICLE_JITTER;

        level.addParticle(particle, x, y, z, 0, 0, 0);
    }

    @Override
    protected Vec3 getModelOffset() {
        return MODEL_OFFSET;
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
    public PoweredFactoryModel<MinerRenderState> getModel() {
        return model;
    }
}