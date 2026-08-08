package io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner;

import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlockEntity;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BiomassBurnerBlockEntity extends GeneratorBlockEntity implements MultiblockControllerAccess, CableAnchorProvider {
    public BiomassBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public Vec3 getCableAnchorPos() {
        return null;
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        return List.of();
    }
}
