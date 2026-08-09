package io.github.stainlessstasis.manifold.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FactoryParticles {
    private FactoryParticles() {}

    public static void spawnJittered(
            Level level, BlockPos anchor, Vec3 worldOffset, double xzJitter,
            RandomSource random, ParticleOptions particle
    ) {
        double x = anchor.getX() + 0.5 + worldOffset.x + (random.nextDouble() - 0.5) * 2 * xzJitter;
        double y = anchor.getY() + 0.5 + worldOffset.y;
        double z = anchor.getZ() + 0.5 + worldOffset.z + (random.nextDouble() - 0.5) * 2 * xzJitter;
        level.addParticle(particle, x, y, z, 0, 0, 0);
    }
}