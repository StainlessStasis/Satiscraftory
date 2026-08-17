package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class SCSoundsProvider extends SoundDefinitionsProvider {
    protected SCSoundsProvider(PackOutput output) {
        super(output, Satiscraftory.MODID);
    }

    @Override
    public void registerSounds() {
        add(SCSounds.MINER_STARTUP, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":miner_startup", SoundDefinition.SoundType.SOUND)
                                .volume(0.67f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                 .subtitle("sound."+Satiscraftory.MODID+".miner_startup")
        );
        add(SCSounds.MINER_DRILLING, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":miner_drilling", SoundDefinition.SoundType.SOUND)
                                .volume(0.67f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".miner_drilling")
        );
        add(SCSounds.MINER_COOLDOWN, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":miner_cooldown", SoundDefinition.SoundType.SOUND)
                                .volume(0.67f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".miner_cooldown")
        );

        add(SCSounds.BIOMASS_BURNER_STARTUP, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":biomass_burner_startup", SoundDefinition.SoundType.SOUND)
                                .volume(0.45f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".biomass_burner_startup")
        );
        add(SCSounds.BIOMASS_BURNER_BURNING, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":biomass_burner_burning", SoundDefinition.SoundType.SOUND)
                                .volume(0.45f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".biomass_burner_burning")
        );
        add(SCSounds.BIOMASS_BURNER_COOLDOWN, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":biomass_burner_cooldown", SoundDefinition.SoundType.SOUND)
                                .volume(0.45f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".biomass_burner_cooldown")
        );

        add(SCSounds.RESOURCE_SCANNER_SCAN, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":resource_scanner_scan", SoundDefinition.SoundType.SOUND)
                                .volume(1f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".resource_scanner_scan")
        );
        add(SCSounds.RESOURCE_SCANNER_PING, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":resource_scanner_ping", SoundDefinition.SoundType.SOUND)
                                .volume(1f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+".resource_scanner_ping")
        );
    }
}
