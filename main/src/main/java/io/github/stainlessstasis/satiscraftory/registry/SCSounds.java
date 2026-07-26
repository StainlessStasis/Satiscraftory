package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SCSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Satiscraftory.MODID);

    public static final Holder<SoundEvent> MINER_STARTUP = SOUND_EVENTS.register(
            "miner_startup",
            SoundEvent::createVariableRangeEvent
    );
    public static final Holder<SoundEvent> MINER_DRILLING = SOUND_EVENTS.register(
            "miner_drilling",
            SoundEvent::createVariableRangeEvent
    );
}
