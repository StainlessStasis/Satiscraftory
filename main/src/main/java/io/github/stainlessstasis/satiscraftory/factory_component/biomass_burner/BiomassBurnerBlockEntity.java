package io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner;

import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlockEntity;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BiomassBurnerBlockEntity extends GeneratorBlockEntity implements MultiblockControllerAccess, CableAnchorProvider {
    public static final Vec3 CABLE_ANCHOR_LOCAL_OFFSET = new Vec3(-10.75, 51.5, -0.75).scale(1/16f);
    private final Vec3 cableAnchorPos;

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
}