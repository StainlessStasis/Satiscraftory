package io.github.stainlessstasis.satiscraftory.factory_component.power_pole;

import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.manifold.factory_power.PowerConduitBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.block.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PowerPoleBlockEntity extends PowerConduitBlockEntity {
    public static final Vec3 CABLE_ANCHOR_LOCAL_OFFSET = new Vec3(0, 3.4, 0);

    public PowerPoleBlockEntity(BlockPos pos, BlockState state) {
        this(SCBlockEntities.POWER_POLE.get(), pos, state);
    }

    public PowerPoleBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState state) {
        super(type, worldPosition, state);
    }

    @Override
    protected MultiblockShape getConduitShape() {
        return PowerPoleBlock.MULTIBLOCK_SHAPE;
    }

    @Override
    protected Vec3 getLocalCableAnchorOffset() {
        return CABLE_ANCHOR_LOCAL_OFFSET;
    }

    @Override
    public int getMaxPowerConnections() {
        return 4;
    }
}
