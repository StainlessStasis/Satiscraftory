package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerAnimations;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import net.minecraft.client.model.Model;
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

import static io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity.PARTICLE_JITTER;

public class MinerRenderer extends MultiblockRenderer<MinerBlockEntity, MinerRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/factory/miner.png");

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

        state.resourceNodeId = blockEntity.getResourceNodeId() != null
                ? blockEntity.getResourceNodeId()
                : SCResourceNodes.IRON.getNodeId();

        spawnDrillParticles(blockEntity, state);
    }

    private void spawnDrillParticles(MinerBlockEntity blockEntity, MinerRenderState state) {
        if (!(blockEntity.getLevel() instanceof Level level)) return;
        if (!blockEntity.spinAnimationState.isStarted()) return;

        long ms = blockEntity.spinAnimationState.getTimeInMillis(state.ageInTicks);
        long deltaTime = ms - blockEntity.getLastParticleTime();
        if (deltaTime >= MinerBlockEntity.PARTICLE_INTERVAL_MS) {
            blockEntity.setLastParticleTime(ms);

            ParticleOptions particle = SCResourceNodes.particleFor(state.resourceNodeId);
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