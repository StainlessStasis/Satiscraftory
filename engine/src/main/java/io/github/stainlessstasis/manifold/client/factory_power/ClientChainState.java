package io.github.stainlessstasis.manifold.client.factory_power;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

/**
 * Clientside mirror of the player's in-progress power link chain
 */
public final class ClientChainState {
    private static volatile @Nullable BlockPos chainStartPos = null;

    private ClientChainState() {}

    public static void setChainStart(@Nullable BlockPos pos) {
        chainStartPos = pos;
    }

    public static @Nullable BlockPos getChainStart() {
        return chainStartPos;
    }
}