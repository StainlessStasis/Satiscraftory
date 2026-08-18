package io.github.stainlessstasis.manifold.network.serverbound;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.factory_component.machine.MachineBlockEntity;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldMachineRecipes;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public record SelectRecipePacket(BlockPos machinePos, Identifier recipeId) implements CustomPacketPayload {
    public static final Type<SelectRecipePacket> TYPE = new Type<>(Manifold.id("select_recipe"));
    private static final double MAX_REACH_SQ = 8d * 8d;

    public static final StreamCodec<ByteBuf, SelectRecipePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SelectRecipePacket::machinePos,
            Identifier.STREAM_CODEC, SelectRecipePacket::recipeId,
            SelectRecipePacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SelectRecipePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            BlockPos pos = packet.machinePos();
            Vec3 center = pos.getCenter();
            if (player.distanceToSqr(center.x(), center.y(), center.z()) > MAX_REACH_SQ) return;

            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (!(blockEntity instanceof MachineBlockEntity machineBE)) return;

            MachineRecipe recipe = ManifoldMachineRecipes.get(packet.recipeId());
            if (recipe == null) return;

            Machine machine = machineBE.getFactoryComponent();
            if (recipe.id().equals(machine.getRecipe().id())) return;
            if (!recipe.machineType().equals(machine.getRecipe().machineType())) return;

            Map<Identifier, Integer> refund = machine.setRecipeWithRefund(recipe, machine.getOutputPorts());
            for (Map.Entry<Identifier, Integer> entry : refund.entrySet()) {
                ItemUtils.giveOrDrop(player, entry.getKey(), entry.getValue());
            }

            // input/output count may have changed - force a fresh menu so slots resync
            player.openMenu(machineBE);
        });
    }
}