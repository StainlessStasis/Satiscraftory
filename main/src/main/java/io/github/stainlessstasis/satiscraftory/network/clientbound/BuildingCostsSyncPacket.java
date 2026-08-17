package io.github.stainlessstasis.satiscraftory.network.clientbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildingCost;
import io.github.stainlessstasis.satiscraftory.building.BuildingCosts;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record BuildingCostsSyncPacket(List<BuildingCost> costs) implements CustomPacketPayload {
    public static final Type<BuildingCostsSyncPacket> TYPE = new Type<>(Satiscraftory.id("building_costs_sync"));

    public static final StreamCodec<ByteBuf, BuildingCostsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BuildingCost.STREAM_CODEC.apply(ByteBufCodecs.list()), BuildingCostsSyncPacket::costs,
            BuildingCostsSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(BuildingCostsSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> BuildingCosts.applyClientSync(packet.costs()));
    }
}