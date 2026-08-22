package io.github.stainlessstasis.satiscraftory.client.building;

import io.github.stainlessstasis.manifold.client.util.BoxOutlineRenderer;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionResolver;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionTarget;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildModeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.awt.Color;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public final class DemolitionPreviewRenderer {
    private static final Color MARKED_COLOR = new Color(0xFF, 0x20, 0x20, 0xF0);
    private static final Color HOVER_COLOR = new Color(0xFF, 0x30, 0x30, 0x80);

    private DemolitionPreviewRenderer() {}

    @SubscribeEvent
    public static void renderDemolishOutlines(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        boolean groupAsLane = LaneBuildModeManager.getClientSide().isLane();

        Set<BlockPos> marked = DemolitionSelectionManager.clientSelection();
        for (BlockPos canonicalPos : marked) {
            DemolitionTarget target = DemolitionResolver.resolve(level, canonicalPos, groupAsLane);
            if (target == null) continue;
            renderOutline(event, target, MARKED_COLOR);
        }

        if (mc.hitResult instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            DemolitionTarget hovered = DemolitionResolver.resolve(level, blockHit.getBlockPos(), groupAsLane);
            if (hovered != null && !marked.contains(hovered.canonicalPos())) {
                renderOutline(event, hovered, HOVER_COLOR);
            }
        }
    }
    
    private static void renderOutline(SubmitCustomGeometryEvent event, DemolitionTarget target, Color color) {
        if (target.isLane()) {
            for (BlockPos pos : target.allPositions()) {
                BoxOutlineRenderer.render(event.getPoseStack(), event.getSubmitNodeCollector(), pos, pos, color);
            }
            return;
        }

        BlockPos min = boundsMin(target.allPositions());
        BlockPos max = boundsMax(target.allPositions());
        BoxOutlineRenderer.render(event.getPoseStack(), event.getSubmitNodeCollector(), min, max, color);
    }

    private static BlockPos boundsMin(List<BlockPos> positions) {
        int x = Integer.MAX_VALUE, y = Integer.MAX_VALUE, z = Integer.MAX_VALUE;
        for (BlockPos pos : positions) {
            x = Math.min(x, pos.getX());
            y = Math.min(y, pos.getY());
            z = Math.min(z, pos.getZ());
        }
        return new BlockPos(x, y, z);
    }

    private static BlockPos boundsMax(List<BlockPos> positions) {
        int x = Integer.MIN_VALUE, y = Integer.MIN_VALUE, z = Integer.MIN_VALUE;
        for (BlockPos pos : positions) {
            x = Math.max(x, pos.getX());
            y = Math.max(y, pos.getY());
            z = Math.max(z, pos.getZ());
        }
        return new BlockPos(x, y, z);
    }
}