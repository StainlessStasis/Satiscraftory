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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.sounds.SoundManager;
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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import static io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity.PARTICLE_JITTER;

public class MinerRenderer extends MultiblockRenderer<MinerBlockEntity, MinerRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/miner.png");
    private static final Map<MinerBlockEntity, WeakReference<MinerDrillSoundInstance>> ACTIVE_DRILL_SOUNDS = new WeakHashMap<>();

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

        updateAnimationPhase(blockEntity, state.ageInTicks, state.gameTime);

        state.startupAnimationState.copyFrom(blockEntity.startupAnimationState);
        state.spinAnimationState.copyFrom(blockEntity.spinAnimationState);
        state.cooldownAnimationState.copyFrom(blockEntity.cooldownAnimationState);
        state.idleAnimationState.copyFrom(blockEntity.idleAnimationState);

        state.resourceNodeId = blockEntity.getResourceNodeId() != null
                ? blockEntity.getResourceNodeId()
                : SCResourceNodes.IRON.getNodeId();

        playDrillSound(blockEntity);
        spawnDrillParticles(blockEntity, state.ageInTicks, state.resourceNodeId);
    }

    private void updateAnimationPhase(MinerBlockEntity miner, float ageInTicks, long gameTime) {
        boolean bufferFull = miner.isBufferFull();

        switch (miner.animationPhase) {
            case STARTUP -> {
                if (!miner.startupAnimationState.isStarted()) {
                    miner.startupAnimationState.start((int) gameTime);
                    playSound(miner, SCSounds.MINER_STARTUP.value());
                    return;
                }
                long ms = miner.startupAnimationState.getTimeInMillis(ageInTicks);
                if (ms >= MinerAnimations.STARTUP.lengthInSeconds() * 1000L) {
                    miner.startupAnimationState.stop();
                    if (bufferFull) {
                        miner.idleAnimationState.start((int) gameTime);
                        miner.animationPhase = MinerBlockEntity.AnimPhase.IDLE;
                    } else {
                        miner.spinAnimationState.start((int) gameTime);
                        miner.animationPhase = MinerBlockEntity.AnimPhase.SPIN;
                    }
                }
            }
            case SPIN -> {
                if (bufferFull) {
                    miner.spinAnimationState.stop();
                    miner.cooldownAnimationState.start((int) gameTime);
                    miner.animationPhase = MinerBlockEntity.AnimPhase.COOLDOWN;
                    playSound(miner, SCSounds.MINER_COOLDOWN.value());
                }
            }
            case COOLDOWN -> {
                long ms = miner.cooldownAnimationState.getTimeInMillis(ageInTicks);
                if (ms >= MinerAnimations.COOLDOWN.lengthInSeconds() * 1000L) {
                    miner.cooldownAnimationState.stop();
                    miner.idleAnimationState.start((int) gameTime);
                    miner.animationPhase = MinerBlockEntity.AnimPhase.IDLE;
                }
            }
            case IDLE -> {
                if (!bufferFull) {
                    miner.idleAnimationState.stop();
                    miner.animationPhase = MinerBlockEntity.AnimPhase.STARTUP;
                }
            }
        }
    }

    private void playSound(MinerBlockEntity blockEntity, SoundEvent sound) {
        if (!(blockEntity.getLevel() instanceof Level level)) return;
        Vec3 pos = blockEntity.getBlockPos().getCenter().add(blockEntity.getParticleOffset());
        level.playLocalSound(pos.x(), pos.y(), pos.z(), sound, SoundSource.BLOCKS, 0.67f, 1f, false);
    }

    private void playDrillSound(MinerBlockEntity blockEntity) {
        if (!blockEntity.spinAnimationState.isStarted()) return;

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        WeakReference<MinerDrillSoundInstance> ref = ACTIVE_DRILL_SOUNDS.get(blockEntity);
        MinerDrillSoundInstance existing = ref != null ? ref.get() : null;
        if (existing != null && soundManager.isActive(existing)) return;

        MinerDrillSoundInstance instance = new MinerDrillSoundInstance(blockEntity, SCSounds.MINER_DRILLING.value());
        soundManager.play(instance);
        ACTIVE_DRILL_SOUNDS.put(blockEntity, new WeakReference<>(instance));
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