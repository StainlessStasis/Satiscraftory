package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerAnimations;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity.PARTICLE_JITTER;

public class MinerRenderer extends MultiblockRenderer<MinerBlockEntity, MinerRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/factory/miner.png");
    public static final long DRILL_SOUND_LOOP_MS = 5000L;

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
            playSound(blockEntity, SCSounds.MINER_STARTUP.value());
        }

        boolean startupEnded = blockEntity.startupAnimationState.getTimeInMillis(state.ageInTicks) >= MinerAnimations.STARTUP.lengthInSeconds() * 1000L;
        if (blockEntity.startupAnimationState.isStarted() && startupEnded) {
            blockEntity.startupAnimationState.stop();
            blockEntity.spinAnimationState.start((int) state.gameTime);
        }

        state.startupAnimationState.copyFrom(blockEntity.startupAnimationState);
        state.spinAnimationState.copyFrom(blockEntity.spinAnimationState);

        state.resourceNodeId = blockEntity.getResourceNodeId() != null
                ? blockEntity.getResourceNodeId()
                : SCResourceNodes.IRON.getNodeId();

        playDrillLoopSound(blockEntity, state.ageInTicks);
        spawnDrillParticles(blockEntity, state.ageInTicks, state.resourceNodeId);
    }

    private void playSound(MinerBlockEntity blockEntity, SoundEvent sound) {
        if (!(blockEntity.getLevel() instanceof Level level)) return;
        Vec3 pos = blockEntity.getBlockPos().getCenter().add(blockEntity.getParticleOffset());
        level.playLocalSound(pos.x(), pos.y(), pos.z(), sound, SoundSource.BLOCKS, 0.7f, 1f, false);
    }

    private void playDrillLoopSound(MinerBlockEntity blockEntity, float ageInTicks) {
        if (!blockEntity.spinAnimationState.isStarted()) return;

        long ms = blockEntity.spinAnimationState.getTimeInMillis(ageInTicks);
        long cycle = ms / DRILL_SOUND_LOOP_MS;
        
        if (cycle != blockEntity.getLastDrillLoopCycle()) {
            blockEntity.setLastDrillLoopCycle(cycle);
            playSound(blockEntity, SCSounds.MINER_DRILLING.value());
        }
    }

    private void spawnDrillParticles(MinerBlockEntity blockEntity, float ageInTicks, Identifier resourceNodeId) {
        if (!(blockEntity.getLevel() instanceof Level level)) return;
        if (!blockEntity.spinAnimationState.isStarted()) return;

        long ms = blockEntity.spinAnimationState.getTimeInMillis(ageInTicks);
        long deltaTime = ms - blockEntity.getLastParticleTime();
        if (deltaTime >= MinerBlockEntity.PARTICLE_INTERVAL_MS) {
            blockEntity.setLastParticleTime(ms);

            ParticleOptions particle = SCResourceNodes.particleFor(resourceNodeId);
            BlockPos pos = blockEntity.getBlockPos();
            Vec3 offset = blockEntity.getParticleOffset();
            RandomSource random = level.getRandom();

            double x = pos.getX() + 0.5 + offset.x + (random.nextDouble() - 0.5) * 2 * PARTICLE_JITTER;
            double y = pos.getY() + 0.25 + offset.y + (random.nextDouble() - 0.5) * 2 * PARTICLE_JITTER;
            double z = pos.getZ() + 0.5 + offset.z + (random.nextDouble() - 0.5) * 2 * PARTICLE_JITTER;

            level.addParticle(particle, x, y, z, 0, 0, 0);
        }
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