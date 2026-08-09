package io.github.stainlessstasis.manifold.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockDevPreview;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class MultiblockCaptureCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Manifold.MODID)
                .then(Commands.literal("preview")
                        .executes(MultiblockCaptureCommands::preview))
                .then(Commands.literal("captureshape")
                        .executes(MultiblockCaptureCommands::captureShape)));
    }

    private static int preview(CommandContext<CommandSourceStack> context) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof Multiblock<?> previewer)) {
            context.getSource().sendFailure(Component.literal("Hold the multiblock's controller item first."));
            return 0;
        }

        BlockPos anchor = player.blockPosition();
        MultiblockDevPreview.activate(anchor, previewer);

        MultiblockShape shape = previewer.getMultiblockShape();
        context.getSource().sendSuccess(() -> Component.literal(
                "Preview anchored at %s (%dx%dx%d, facing NORTH). Scaffold filled cells, then run /manifold captureshape to log air blocks as a set of unfilled offsets."
                        .formatted(anchor.toShortString(), shape.width(), shape.height(), shape.depth())
        ), false);
        return 1;
    }

    @SuppressWarnings("DataFlowIssue") // isActive() already ensures multiblock shape is not null
    private static int captureShape(CommandContext<CommandSourceStack> context) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return 0;

        if (!MultiblockDevPreview.isActive()) {
            context.getSource().sendFailure(Component.literal("No active preview. Run /manifold preview first."));
            return 0;
        }

        BlockPos anchor = MultiblockDevPreview.anchorPos();
        MultiblockShape shape = MultiblockDevPreview.activeShape();
        BlockPos min = shape.canonicalMin();
        BlockPos max = shape.canonicalMax();

        StringBuilder builder = new StringBuilder();
        builder.append("public static final Set<BlockPos> UNFILLED_OFFSETS = Set.of(\n");
        int unfilled = 0;
        int total = 0;

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos canonical = new BlockPos(x, y, z);
                    if (canonical.equals(BlockPos.ZERO)) continue; // controller cell
                    total++;

                    BlockPos world = anchor.offset(canonical);
                    if (level.getBlockState(world).isAir()) {
                        builder.append("    new BlockPos(").append(x).append(", ").append(y).append(", ").append(z).append("),\n");
                        unfilled++;
                    }
                }
            }
        }
        builder.append(");");

        Manifold.LOGGER.info("[MultiblockDevPreview] Captured shape at {} ({} unfilled / {} total cells):\n{}",
                anchor, unfilled, total, builder);

        MultiblockDevPreview.clear();

        int finalUnfilled = unfilled;
        int finalTotal = total;
        context.getSource().sendSuccess(() -> Component.literal(
                "Captured %d unfilled / %d total cells. Check the log for the Set<BlockPos> literal."
                        .formatted(finalUnfilled, finalTotal)
        ), false);
        return unfilled;
    }
}