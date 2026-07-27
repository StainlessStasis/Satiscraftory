package io.github.stainlessstasis.satiscraftory.world;

import com.mojang.serialization.Codec;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class SpawnGuaranteeData extends SavedData {
    public static final SavedDataType<SpawnGuaranteeData> TYPE = new SavedDataType<>(
            Satiscraftory.id("spawn_guarantee"),
            SpawnGuaranteeData::new,
            Codec.BOOL.xmap(SpawnGuaranteeData::new, SpawnGuaranteeData::isIronGuaranteed)
    );

    private boolean ironGuaranteed;

    public SpawnGuaranteeData() {
        this(false);
    }

    private SpawnGuaranteeData(boolean ironGuaranteed) {
        this.ironGuaranteed = ironGuaranteed;
    }

    public static SpawnGuaranteeData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isIronGuaranteed() {
        return ironGuaranteed;
    }

    public void markIronGuaranteed() {
        ironGuaranteed = true;
        setDirty();
    }
}