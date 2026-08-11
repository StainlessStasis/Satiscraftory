package io.github.stainlessstasis.satiscraftory.progression;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Per-tier unlocks for smaller progression steps (e.g. unlocking logistics and part assembly separately, but still within the same tier)
 * <br><br> Currently unused, but will be implemented in full release
 */
public enum TierUnlock {
    UNLOCKED_BY_DEFAULT;

    public static final Codec<TierUnlock> CODEC = Codec.STRING.xmap(TierUnlock::valueOf, Enum::name);

    public static final StreamCodec<ByteBuf, TierUnlock> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(TierUnlock::valueOf, Enum::name);
}