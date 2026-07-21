package io.github.stainlessstasis.manifold.network;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.block_entity.factory_component.BeltBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltLane;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record BeltSyncPacket(List<Entry> entries) implements CustomPacketPayload {

    public record Entry(BlockPos headBlockPos, List<BlockPos> laneBlocks, long syncTick,
                        List<BeltLane.ItemSnapshot> items, boolean frontJammed) {

        static final StreamCodec<ByteBuf, ItemSnapshotWire> ITEM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, w -> w.id,
                ByteBufCodecs.DOUBLE, w -> w.position,
                Identifier.STREAM_CODEC, w -> w.itemId,
                ItemSnapshotWire::new
        );

        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Entry::headBlockPos,
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), Entry::laneBlocks,
                ByteBufCodecs.VAR_LONG, Entry::syncTick,
                ITEM_CODEC.apply(ByteBufCodecs.list()).map(
                        list -> list.stream().map(w -> new BeltLane.ItemSnapshot(w.id, w.position, w.itemId)).toList(),
                        list -> list.stream().map(s -> new ItemSnapshotWire(s.id(), s.position(), s.itemId())).toList()
                ), Entry::items,
                ByteBufCodecs.BOOL, Entry::frontJammed,
                Entry::new
        );

        record ItemSnapshotWire(long id, double position, Identifier itemId) {}
    }

    public static final Type<BeltSyncPacket> TYPE = new Type<>(Manifold.id("belt_sync"));

    public static final StreamCodec<ByteBuf, BeltSyncPacket> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), BeltSyncPacket::entries,
            BeltSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(BeltSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            for (Entry entry : packet.entries()) {
                for (BlockPos pos : entry.laneBlocks()) {
                    if (level.getBlockEntity(pos) instanceof BeltBlockEntity beltBE) {
                        beltBE.applySync(entry.laneBlocks(), entry.items(), entry.syncTick(), entry.frontJammed());
                    }
                }
            }
        });
    }
}