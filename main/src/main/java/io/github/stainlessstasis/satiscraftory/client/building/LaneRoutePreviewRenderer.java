package io.github.stainlessstasis.satiscraftory.client.building;

import io.github.stainlessstasis.manifold.client.block_preview.BlockPreviewSubmission;
import io.github.stainlessstasis.manifold.client.block_preview.PlacementPreview;
import io.github.stainlessstasis.manifold.factory.LaneManager;
import io.github.stainlessstasis.manifold.factory_component.Laneable;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltLaneRouter;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildModeManager;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneMarker;
import io.github.stainlessstasis.satiscraftory.network.serverbound.LaneAxisHintPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public final class LaneRoutePreviewRenderer {
    private LaneRoutePreviewRenderer() {}

    private static @Nullable BlockPos hysteresisStart;
    private static @Nullable Boolean hysteresisPrimaryIsX;
    private static @Nullable Boolean lastSentPrimaryIsX;
    
    private static BeltLaneRouter.@Nullable LaneRoute previewedRoute;
    public static BeltLaneRouter.@Nullable LaneRoute currentPreview() {
        return previewedRoute;
    }

    @SubscribeEvent
    static void render(SubmitCustomGeometryEvent event) {
        previewedRoute = null;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) return;

        BlockPos start = LaneMarker.getClientSide();
        if (start == null) {
            hysteresisStart = null;
            hysteresisPrimaryIsX = null;
            lastSentPrimaryIsX = null;
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;
        if (!LaneBuildModeManager.getClientSide().isLane()) return;

        BlockItem selected = BuildGunItem.getSelectedBlockItemClientSide();
        if (!(selected.getBlock() instanceof Laneable)) return;

        if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        ItemStack dummyStack = new ItemStack(selected);
        UseOnContext useContext = new UseOnContext(level, player, InteractionHand.MAIN_HAND, dummyStack, blockHit);
        BlockPlaceContext placeContext = new BlockPlaceContext(useContext);
        if (!placeContext.canPlace()) return;

        BlockPos end = placeContext.getClickedPos();

        //noinspection PointlessNullCheck
        if (hysteresisStart == null || !start.equals(hysteresisStart)) {
            hysteresisStart = start;
            hysteresisPrimaryIsX = null;
        }

        BeltLaneRouter.LaneRoute route = BeltLaneRouter.route(start, end, hysteresisPrimaryIsX);
        hysteresisPrimaryIsX = route.primaryIsX();
        previewedRoute = route;

        if (!hysteresisPrimaryIsX.equals(lastSentPrimaryIsX)) {
            lastSentPrimaryIsX = hysteresisPrimaryIsX;
            ClientPacketDistributor.sendToServer(new LaneAxisHintPacket(hysteresisPrimaryIsX));
        }

        boolean routeOk = route.feasible() && route.length() <= LaneManager.MAX_LANE_LENGTH;

        for (BlockPos pos : route.positions()) {
            Color tint = (routeOk && BeltLaneRouter.canOccupy(level, pos)) ? PlacementPreview.VALID_COLOR : PlacementPreview.INVALID_COLOR;
            BlockPreviewSubmission.submitBox(event.getPoseStack(), event.getSubmitNodeCollector(), pos, tint);
        }
    }
}