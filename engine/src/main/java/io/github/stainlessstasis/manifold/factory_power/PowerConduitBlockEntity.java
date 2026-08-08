package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.network.PowerGrid;
import io.github.stainlessstasis.manifold.factory_power.network.PowerLinkable;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class PowerConduitBlockEntity extends BlockEntity implements MultiblockControllerAccess, CableAnchorProvider, PowerLinkable {
    public PowerConduitBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState state) {
        super(type, worldPosition, state);
        this.cableAnchorPos = new Vec3(getBlockPos()).add(getCableOffset(state, getLocalCableAnchorOffset()));
    }

    protected abstract MultiblockShape getConduitShape();
    protected abstract Vec3 getLocalCableAnchorOffset();
    private final Vec3 cableAnchorPos;

    @Override
    public Vec3 getCableAnchorPos() {
        return cableAnchorPos;
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return getConduitShape().absoluteFillerPositions(getBlockPos(), facing);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;
        GlobalPos pos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        PowerGrid grid = FactoryNetwork.get(serverLevel).getPowerGrid();
        grid.addNode(pos);
        grid.setMaxConnections(pos, getMaxPowerConnections());
    }
}