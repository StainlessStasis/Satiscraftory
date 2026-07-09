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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BeltBlockEntity extends BlockEntity {
    public static final int LENGTH_TICKS = 60;
    public static final double MIN_GAP = 0.15;

    private Belt belt;
    private List<Belt.ItemSnapshot> renderItems = List.of();

    private long lastSyncTick = 0;
    private int previousItemCount = -1;
    private boolean previousFrontAtExit = false;

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

    public long getLastSyncTick() {
        return lastSyncTick;
    }

    /**
     * Only handles render sync. The belt's actual state is simulated via FactoryNetwork.tickAll(), independent of chunk loading
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BeltBlockEntity blockEntity) {
        Belt belt = blockEntity.belt;
        if (belt == null) return;

        int itemCount = belt.getItemCount();
        boolean frontAtExit = belt.isFrontAtExit();

        boolean needsSync = itemCount != blockEntity.previousItemCount
                || frontAtExit != blockEntity.previousFrontAtExit;

        blockEntity.previousItemCount = itemCount;
        blockEntity.previousFrontAtExit = frontAtExit;

        if (!needsSync) return;

        blockEntity.renderItems = belt.getItemSnapshots();
        blockEntity.lastSyncTick = level.getGameTime();
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
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
        tag.putLong("syncTick", lastSyncTick);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NonNull ValueInput input) {
        super.handleUpdateTag(input);
        parseRenderItems(input);
    }

    @Override
    public void onDataPacket(@NonNull Connection connection, @NonNull ValueInput input) {
        super.onDataPacket(connection, input);
        parseRenderItems(input);
    }

    private void parseRenderItems(ValueInput input) {
        List<Belt.ItemSnapshot> parsedItems = new ArrayList<>();
        for (ValueInput itemInput : input.childrenListOrEmpty("renderItems")) {
            double position = itemInput.getDoubleOr("position", 0);
            String typeId = itemInput.getStringOr("typeId", "");
            parsedItems.add(new Belt.ItemSnapshot(position, typeId));
        }
        renderItems = parsedItems;
        lastSyncTick = input.getLongOr("syncTick", lastSyncTick);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}