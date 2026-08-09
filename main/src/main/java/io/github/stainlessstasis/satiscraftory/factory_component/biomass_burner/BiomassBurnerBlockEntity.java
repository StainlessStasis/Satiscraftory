package io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner;

import io.github.stainlessstasis.manifold.animation.AnimationPhaseTransition;
import io.github.stainlessstasis.manifold.factory_component.generator.Generator;
import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlockEntity;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.animation.AnimationPhase;
import io.github.stainlessstasis.manifold.animation.PhasedAnimationStates;
import io.github.stainlessstasis.manifold.util.DirectionalOffset;
import io.github.stainlessstasis.manifold.util.FactorySounds;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import io.github.stainlessstasis.manifold.util.TickDebouncer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BiomassBurnerBlockEntity extends GeneratorBlockEntity
        implements MultiblockControllerAccess, CableAnchorProvider, AnimationPhaseTransition {
    public static final Vec3 CABLE_ANCHOR_LOCAL_OFFSET = new Vec3(-10.75, 51.5, -0.75).scale(1/16f);
    private final Vec3 cableAnchorPos;

    public static final Vec3 SOUND_LOCAL_OFFSET = new Vec3(0, 0, -2);
    public static final Vec3 LEAF_PARTICLE_LOCAL_OFFSET = new Vec3(0, 0.4, -2);
    public static final Vec3 SMOKE_PARTICLE_LOCAL_OFFSET = new Vec3(0, 2.5, -0.5);

    private final Vec3 soundOffset;
    private final Vec3 leafParticleOffset;
    private final Vec3 smokeParticleOffset;

    public final PhasedAnimationStates animationStates = new PhasedAnimationStates();
    public AnimationPhase animationPhase = AnimationPhase.IDLE;

    private static final int BURN_GRACE_TICKS = 60;
    private final TickDebouncer burningDebouncer = new TickDebouncer(false, 1, BURN_GRACE_TICKS);

    public BiomassBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(SCBlockEntities.BIOMASS_BURNER.get(), pos, state);
        this.cableAnchorPos = new Vec3(getBlockPos()).add(getCableOffset(state, CABLE_ANCHOR_LOCAL_OFFSET));

        Direction facing = DirectionalOffset.facingOf(state);
        this.soundOffset = DirectionalOffset.toWorld(facing, SOUND_LOCAL_OFFSET);
        this.leafParticleOffset = DirectionalOffset.toWorld(facing, LEAF_PARTICLE_LOCAL_OFFSET);
        this.smokeParticleOffset = DirectionalOffset.toWorld(facing, SMOKE_PARTICLE_LOCAL_OFFSET);
    }

    @Override
    public Vec3 getCableAnchorPos() {
        return cableAnchorPos;
    }

    public Vec3 getSoundOffset() {
        return soundOffset;
    }

    public Vec3 getLeafParticleOffset() {
        return leafParticleOffset;
    }

    public Vec3 getSmokeParticleOffset() {
        return smokeParticleOffset;
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return BiomassBurnerBlock.MULTIBLOCK_SHAPE.absoluteFillerPositions(getBlockPos(), facing);
    }

    public boolean isEffectivelyBurning() {
        return burningDebouncer.get();
    }

    @Override
    public void onEnterStartup(long gameTime) {
        FactorySounds.playLocal(this, soundOffset, SCSounds.BIOMASS_BURNER_STARTUP.value(), 1f, 1f);
    }

    @Override
    public void onEnterCooldown(long gameTime) {
        FactorySounds.playLocal(this, soundOffset, SCSounds.BIOMASS_BURNER_COOLDOWN.value(), 1f, 1f);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, BiomassBurnerBlockEntity burner) {
        GeneratorBlockEntity.serverTick(level, pos, state, burner);

        Generator generator = burner.getFactoryComponent();
        if (generator == null) return;

        if (burner.burningDebouncer.update(generator.isBurning())) {
            burner.syncToClients(level);
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("EffectivelyBurning", burningDebouncer.get());
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        burningDebouncer.restore(input.getBooleanOr("EffectivelyBurning", false));
    }

    private void syncToClients(ServerLevel level) {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}