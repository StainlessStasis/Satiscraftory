package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.AbstractFactoryBlock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.manifold.util.DirectionalOffset;
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
        Direction facing = AbstractFactoryBlock.facingOf(state);
        this.cableAnchorOffset = DirectionalOffset.toWorld(facing, getLocalCableAnchorOffset());
    }

    protected abstract MultiblockShape getConduitShape();
    protected abstract Vec3 getLocalCableAnchorOffset();
    private final Vec3 cableAnchorOffset;

    @Override
    public Vec3 getCableAnchorPos() {
        return Vec3.atLowerCornerOf(getBlockPos()).add(cableAnchorOffset);
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

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            FactoryNetwork.get(serverLevel).getPowerGrid()
                .removeNode(GlobalPos.of(serverLevel.dimension(), getBlockPos()));
        }
    }
}