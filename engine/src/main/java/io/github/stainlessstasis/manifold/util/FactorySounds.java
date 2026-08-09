package io.github.stainlessstasis.manifold.util;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class FactorySounds {
    private FactorySounds() {}

    public static void playLocal(BlockEntity blockEntity, Vec3 worldOffset, SoundEvent sound) {
        playLocal(blockEntity, worldOffset, sound, 1f, 1f);
    }

    public static void playLocal(BlockEntity blockEntity, Vec3 worldOffset, SoundEvent sound, float volume, float pitch) {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        Vec3 pos = blockEntity.getBlockPos().getCenter().add(worldOffset);
        level.playLocalSound(pos.x(), pos.y(), pos.z(), sound, SoundSource.BLOCKS, volume, pitch, false);
    }
}