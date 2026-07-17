package io.github.stainlessstasis.manifold.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public final class FactoryClientCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("factory")
                .then(Commands.literal("rendered")
                        .executes(FactoryClientCommands::reportRendered))
        );
    }

    private static int reportRendered(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            ctx.getSource().sendSuccess(() -> Component.literal("No client level loaded."), false);
            return 0;
        }

        int belts = 0, producers = 0, consumers = 0, machines = 0, containers = 0;

        int radius = minecraft.options.renderDistance().get();
        ChunkPos center = minecraft.player.chunkPosition();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = center.x() + dx;
                int chunkZ = center.z() + dz;
                if (!level.hasChunk(chunkX, chunkZ)) continue;

                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity.getType() == ManifoldBlockEntities.BELT.get()) belts++;
                    else if (blockEntity.getType() == ManifoldBlockEntities.PRODUCER.get()) producers++;
                    else if (blockEntity.getType() == ManifoldBlockEntities.CONSUMER.get()) consumers++;
                    else if (blockEntity.getType() == ManifoldBlockEntities.MACHINE.get()) machines++;
                    else if (blockEntity.getType() == ManifoldBlockEntities.CONTAINER.get()) containers++;
                }
            }
        }

        int total = belts + producers + consumers + machines + containers;
        final int fBelts = belts, fProducers = producers, fConsumers = consumers, fMachines = machines, fContainers = containers, fTotal = total;

        ctx.getSource().sendSuccess(() -> Component.literal(
                ("""
                        
                        Factory components in rendered chunks (client):
                        Belts: %d, Producers: %d, Consumers: %d, Machines: %d, Containers: %d
                        Total: %d""")
                        .formatted(fBelts, fProducers, fConsumers, fMachines, fContainers, fTotal)), false);
        return total;
    }
}