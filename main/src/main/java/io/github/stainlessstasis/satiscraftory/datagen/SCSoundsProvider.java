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
                                .volume(1f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .stream()
                                .preload()
                )
                 .subtitle("sound."+Satiscraftory.MODID+"miner_startup")
        );
        add(SCSounds.MINER_DRILLING, SoundDefinition.definition()
                .with(
                        sound(Satiscraftory.MODID+":miner_drilling", SoundDefinition.SoundType.SOUND)
                                .volume(1f)
                                .pitch(1f)
                                .attenuationDistance(16)
                                .stream()
                                .preload()
                )
                .subtitle("sound."+Satiscraftory.MODID+"miner_drilling")
        );
    }
}
