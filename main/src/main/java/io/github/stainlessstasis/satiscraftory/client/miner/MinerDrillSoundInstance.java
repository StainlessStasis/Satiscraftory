package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;

public class MinerDrillSoundInstance extends AbstractTickableSoundInstance {
    private final WeakReference<MinerBlockEntity> minerRef;

    public MinerDrillSoundInstance(MinerBlockEntity miner, SoundEvent sound) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        minerRef = new WeakReference<>(miner);
        Vec3 pos = miner.getBlockPos().getCenter().add(miner.getParticleOffset());
        x = pos.x();
        y = pos.y();
        z = pos.z();
        looping = true;
        delay = 0;
        volume = 0.67f;
        pitch = 1f;
    }

    @Override
    public void tick() {
        MinerBlockEntity miner = minerRef.get();
        if (miner == null || miner.isRemoved() || !miner.spinAnimationState.isStarted()) {
            stop();
        }
    }
}