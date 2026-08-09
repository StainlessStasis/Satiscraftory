package io.github.stainlessstasis.satiscraftory.client.biomass_burner;

import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;

public class BiomassBurningSoundInstance extends AbstractTickableSoundInstance {
    private final WeakReference<BiomassBurnerBlockEntity> burnerRef;

    public BiomassBurningSoundInstance(BiomassBurnerBlockEntity burner, SoundEvent sound) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        burnerRef = new WeakReference<>(burner);
        Vec3 pos = burner.getBlockPos().getCenter().add(burner.getSoundOffset());
        x = pos.x();
        y = pos.y();
        z = pos.z();
        looping = true;
        delay = 0;
        volume = 1f;
        pitch = 1f;
    }

    @Override
    public void tick() {
        BiomassBurnerBlockEntity burner = burnerRef.get();
        if (burner == null || burner.isRemoved() || !burner.animationStates.loop.isStarted()) {
            stop();
        }
    }
}