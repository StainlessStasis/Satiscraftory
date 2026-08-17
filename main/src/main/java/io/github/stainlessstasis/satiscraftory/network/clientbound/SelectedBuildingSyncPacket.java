package io.github.stainlessstasis.satiscraftory.network.clientbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record SelectedBuildingSyncPacket(Identifier selectedId) implements CustomPacketPayload {
    public static final Type<SelectedBuildingSyncPacket> TYPE = new Type<>(Satiscraftory.id("selected_building_sync"));

    public static final StreamCodec<ByteBuf, SelectedBuildingSyncPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SelectedBuildingSyncPacket::selectedId,
            SelectedBuildingSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SelectedBuildingSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> BuildGunItem.applyClientSync(packet.selectedId()));
    }
}