package io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner;

import io.github.stainlessstasis.manifold.factory_component.generator.Generator;
import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlockEntity;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.satiscraftory.animation.AnimationPhase;
import io.github.stainlessstasis.satiscraftory.animation.PhasedAnimationStates;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.util.TickDebouncer;
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

public class BiomassBurnerBlockEntity extends GeneratorBlockEntity implements MultiblockControllerAccess, CableAnchorProvider {
    public static final Vec3 CABLE_ANCHOR_LOCAL_OFFSET = new Vec3(-10.75, 51.5, -0.75).scale(1/16f);
    private final Vec3 cableAnchorPos;

    public final PhasedAnimationStates animationStates = new PhasedAnimationStates();
    public AnimationPhase animationPhase = AnimationPhase.IDLE;

    private static final int BURN_GRACE_TICKS = 60;
    private final TickDebouncer burningDebouncer = new TickDebouncer(false, 1, BURN_GRACE_TICKS);

    public BiomassBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(SCBlockEntities.BIOMASS_BURNER.get(), pos, state);
        this.cableAnchorPos = new Vec3(getBlockPos()).add(getCableOffset(state, CABLE_ANCHOR_LOCAL_OFFSET));
    }

    @Override
    public Vec3 getCableAnchorPos() {
        return cableAnchorPos;
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return BiomassBurnerBlock.MULTIBLOCK_SHAPE.absoluteFillerPositions(getBlockPos(), facing);
    }

    public boolean isEffectivelyBurning() {
        return burningDebouncer.get();
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