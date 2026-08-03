package io.github.stainlessstasis.manifold.network;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.factory_power.ClientPowerGrid;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record PowerGridSyncPacket(List<Entry> edges) implements CustomPacketPayload {

    public record Entry(BlockPos posA, BlockPos posB) {
        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Entry::posA,
                BlockPos.STREAM_CODEC, Entry::posB,
                Entry::new
        );
    }

    public static final Type<PowerGridSyncPacket> TYPE = new Type<>(Manifold.id("power_grid_sync"));

    public static final StreamCodec<ByteBuf, PowerGridSyncPacket> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), PowerGridSyncPacket::edges,
            PowerGridSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(PowerGridSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPowerGrid.applySync(packet.edges()));
    }
}