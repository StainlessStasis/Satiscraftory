package io.github.stainlessstasis.manifold.client.block_preview;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public final class PlacementPreviewChecker {
    private static Predicate<Context> predicate = _ -> true;

    private PlacementPreviewChecker() {}

    public record Context(Level level, BlockPos pos, boolean isValid) {}

    public static void register(Predicate<Context> predicate) {
        PlacementPreviewChecker.predicate = predicate;
    }

    public static boolean isPreviewable(Context context) {
        return predicate.test(context);
    }

    public static boolean isPreviewable(Level level, BlockPos pos, boolean isValid) {
        return isPreviewable(new Context(level, pos, isValid));
    }
}