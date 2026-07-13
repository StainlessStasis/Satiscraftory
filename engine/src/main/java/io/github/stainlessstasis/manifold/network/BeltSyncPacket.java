package io.github.stainlessstasis.manifold.network;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_component.Belt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record BeltSyncPacket(List<Entry> entries) implements CustomPacketPayload {
    public static final Type<BeltSyncPacket> TYPE = new Type<>(Manifold.id("belt_sync"));

    private static final StreamCodec<ByteBuf, Belt.ItemSnapshot> ITEM_SNAPSHOT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, snapshot -> (float) snapshot.position(),
            ByteBufCodecs.STRING_UTF8, Belt.ItemSnapshot::typeId,
            (position, typeId) -> new Belt.ItemSnapshot(position, typeId)
    );

    private static final StreamCodec<ByteBuf, List<Belt.ItemSnapshot>> ITEM_SNAPSHOT_LIST_STREAM_CODEC =
            ByteBufCodecs.collection(ArrayList::new, ITEM_SNAPSHOT_STREAM_CODEC);

    public record Entry(BlockPos pos, long syncTick, List<Belt.ItemSnapshot> items) {
        static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Entry::pos,
                ByteBufCodecs.VAR_LONG, Entry::syncTick,
                ITEM_SNAPSHOT_LIST_STREAM_CODEC, Entry::items,
                Entry::new
        );
    }

    private static final StreamCodec<ByteBuf, List<Entry>> ENTRIES_STREAM_CODEC =
            ByteBufCodecs.collection(ArrayList::new, Entry.STREAM_CODEC);

    public static final StreamCodec<ByteBuf, BeltSyncPacket> STREAM_CODEC =
            ENTRIES_STREAM_CODEC.map(BeltSyncPacket::new, BeltSyncPacket::entries);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
