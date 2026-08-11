package io.github.stainlessstasis.satiscraftory.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TierUnlockData extends SavedData {
    private static final int STARTING_TIER = 1;
    private static final Set<TierUnlock> STARTING_UNLOCKS = EnumSet.of(
            TierUnlock.UNLOCKED_BY_DEFAULT
    );

    public static final SavedDataType<TierUnlockData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Satiscraftory.MODID, "tier_unlocks"),
            TierUnlockData::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("tier").forGetter(unlockData -> unlockData.tier),
                    TierUnlock.CODEC.listOf().fieldOf("unlocks").forGetter(unlockData -> List.copyOf(unlockData.unlocks))
            ).apply(instance, TierUnlockData::fromLoaded))
    );

    private int tier = STARTING_TIER;
    private final Set<TierUnlock> unlocks = EnumSet.copyOf(STARTING_UNLOCKS);

    public TierUnlockData() {}

    public static TierUnlockData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    private static TierUnlockData fromLoaded(int tier, List<TierUnlock> unlocks) {
        TierUnlockData data = new TierUnlockData();
        data.tier = tier;
        data.unlocks.clear();
        data.unlocks.addAll(unlocks);
        return data;
    }

    public int tier() {
        return tier;
    }

    public void setTier(int tier) {
        if (this.tier != tier) {
            this.tier = tier;
            setDirty();
        }
    }

    public boolean isUnlocked(TierUnlock unlock) {
        return unlocks.contains(unlock);
    }

    public Set<TierUnlock> unlocks() {
        return Set.copyOf(unlocks);
    }

    /**
     * @return true if this actually changed anything (i.e. it wasn't already unlocked)
     */
    public boolean unlock(TierUnlock unlock) {
        boolean changed = unlocks.add(unlock);
        if (changed) setDirty();
        return changed;
    }
}