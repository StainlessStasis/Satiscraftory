package com.example.examplemod.engine_internal.block_entity;

import com.example.examplemod.engine_internal.Belt;
import com.example.examplemod.engine_internal.factory.FactoryLinking;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BeltBlockEntity extends BlockEntity {
    private static final int LENGTH_TICKS = 10;
    private static final double MIN_GAP = 0.15;

    private Belt belt;
    private List<Belt.ItemSnapshot> renderItems = List.of();

    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(InternalEngineBlockEntities.BELT.get(), pos, state);
    }

    public BeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        belt = network.getOrCreateBelt(getBlockPos(), () -> new Belt(LENGTH_TICKS, MIN_GAP));

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        network.linkBeltOutput(getBlockPos(), resolveOutputPos());
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) {
            relink(FactoryNetwork.get(serverLevel));
        }
    }

    private BlockPos resolveOutputPos() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return getBlockPos().relative(facing);
    }

    public Belt getBelt() {
        return belt;
    }

    public List<Belt.ItemSnapshot> getRenderItems() {
        return renderItems;
    }

    /**
     * Only handles render sync. The belt's actual state is simulated via FactoryNetwork.tickAll(), independent of chunk loading
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BeltBlockEntity blockEntity) {
        if (blockEntity.belt == null) return;

        blockEntity.renderItems = blockEntity.belt.getItemSnapshots();
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        ListTag itemsTag = new ListTag();
        for (Belt.ItemSnapshot itemSnapshot : renderItems) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putDouble("position", itemSnapshot.position());
            itemTag.putString("typeId", itemSnapshot.typeId());
            itemsTag.add(itemTag);
        }
        tag.put("renderItems", itemsTag);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NonNull ValueInput input) {
        super.handleUpdateTag(input);
        List<Belt.ItemSnapshot> parsedItems = new ArrayList<>();
        for (ValueInput itemInput : input.childrenListOrEmpty("renderItems")) {
            double position = itemInput.getDoubleOr("position", 0);
            String typeId = itemInput.getStringOr("typeId", "");
            parsedItems.add(new Belt.ItemSnapshot(position, typeId));
        }
        renderItems = parsedItems;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}


