package io.github.stainlessstasis.satiscraftory.world;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.world.resource_node.ResourceNodeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber
public class SpawnGuaranteeHandler {
    private static final int MIN_DISTANCE = 100;
    private static final int MAX_DISTANCE = 300;
    private static final int MAX_PLACEMENT_ATTEMPTS = 20;

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().overworld();
        SpawnGuaranteeData data = SpawnGuaranteeData.get(overworld);
        if (data.isIronGuaranteed()) return;

        // TODO: idk if this is the ACTUAL world spawn, but its the only spawn point i could find so it is what it is
        BlockPos spawnPos = overworld.getServer().getRespawnData().pos();
        RandomSource random = RandomSource.create(overworld.getSeed());

        boolean placed = false;
        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS && !placed; attempt++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            int dist = random.nextIntBetweenInclusive(MIN_DISTANCE, MAX_DISTANCE);
            BlockPos candidate = spawnPos.offset(
                    (int) Math.round(Math.cos(angle) * dist),
                    0,
                    (int) Math.round(Math.sin(angle) * dist)
            );

            overworld.getChunkAt(candidate);
            placed = ResourceNodeFeature.placeCluster(
                    overworld, random,
                    SCResourceNodes.IRON.toConfig().withClusterSize(UniformInt.of(3, 3)).withClusterSpread(UniformInt.of(15, 25)),
                    candidate
            );
        }

        if (!placed) {
            Satiscraftory.LOGGER.warn("Failed to place guaranteed iron node near spawn after {} attempts", MAX_PLACEMENT_ATTEMPTS);
        }

        data.markIronGuaranteed();
    }
}