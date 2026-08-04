package io.github.stainlessstasis.satiscraftory.factory_component.power_pole;

import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.factory_power.PowerLinkable;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.util.DirectionalOffset;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PowerPoleBlockEntity extends BlockEntity implements MultiblockControllerAccess, CableAnchorProvider, PowerLinkable {
    private final Vec3 cableAnchorOffset;

    public PowerPoleBlockEntity(BlockPos pos, BlockState state) {
        this(SCBlockEntities.POWER_POLE.get(), pos, state);
    }

    public PowerPoleBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState state) {
        super(type, worldPosition, state);
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        this.cableAnchorOffset = DirectionalOffset.toWorld(facing, new Vec3(0, 2.8, 0));
    }

    @Override
    public Vec3 getCableAnchorPos() {
        return cableAnchorOffset;
    }

    @Override
    public int getMaxPowerConnections() {
        return 4;
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return PowerPoleBlock.MULTIBLOCK_SHAPE.absoluteFillerPositions(getBlockPos(), facing);
    }
}
