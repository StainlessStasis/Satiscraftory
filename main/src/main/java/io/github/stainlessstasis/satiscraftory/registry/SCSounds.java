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
    public static final Holder<SoundEvent> MINER_COOLDOWN = SOUND_EVENTS.register(
            "miner_cooldown",
            SoundEvent::createVariableRangeEvent
    );

    public static final Holder<SoundEvent> BIOMASS_BURNER_STARTUP = SOUND_EVENTS.register(
            "biomass_burner_startup",
            SoundEvent::createVariableRangeEvent
    );
    public static final Holder<SoundEvent> BIOMASS_BURNER_BURNING = SOUND_EVENTS.register(
            "biomass_burner_burning",
            SoundEvent::createVariableRangeEvent
    );
    public static final Holder<SoundEvent> BIOMASS_BURNER_COOLDOWN = SOUND_EVENTS.register(
            "biomass_burner_cooldown",
            SoundEvent::createVariableRangeEvent
    );

    public static final Holder<SoundEvent> RESOURCE_SCANNER_SCAN = SOUND_EVENTS.register(
            "resource_scanner_scan",
            SoundEvent::createVariableRangeEvent
    );
    public static final Holder<SoundEvent> RESOURCE_SCANNER_PING = SOUND_EVENTS.register(
            "resource_scanner_ping",
            SoundEvent::createVariableRangeEvent
    );

    public static final Holder<SoundEvent> BUILD_GUN_DEMOLISH = SOUND_EVENTS.register(
            "build_gun_demolish",
            SoundEvent::createVariableRangeEvent
    );
}
