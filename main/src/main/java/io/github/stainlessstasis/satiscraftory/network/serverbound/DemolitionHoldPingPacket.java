package io.github.stainlessstasis.satiscraftory.network.serverbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record DemolitionHoldPingPacket() implements CustomPacketPayload {
    public static final Type<DemolitionHoldPingPacket> TYPE = new Type<>(Satiscraftory.id("demolition_hold_ping"));

    public static final StreamCodec<ByteBuf, DemolitionHoldPingPacket> STREAM_CODEC = StreamCodec.unit(new DemolitionHoldPingPacket());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(DemolitionHoldPingPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        context.enqueueWork(() -> DemolitionSelectionManager.onHoldPing(player));
    }
}