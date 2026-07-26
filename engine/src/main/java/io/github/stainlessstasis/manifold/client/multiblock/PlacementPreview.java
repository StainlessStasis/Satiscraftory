package io.github.stainlessstasis.manifold.client.multiblock;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPreviewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class PlacementPreview {

    @SubscribeEvent
    public static void renderPreview(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof MultiblockPreviewer<?> multiblockPreviewer)) return;
        if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        UseOnContext useContext = new UseOnContext(player.level(), player, InteractionHand.MAIN_HAND, held, blockHit);
        BlockPlaceContext placeContext = new BlockPlaceContext(useContext);
        if (!placeContext.canPlace()) return;

        BaseEntityBlock block = multiblockPreviewer.getPreviewBlock();
        BlockState previewState = multiblockPreviewer.getPreviewPlacement(placeContext);
        if (previewState == null) return;

        BlockPos origin = placeContext.getClickedPos();
        Direction facing = previewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean valid = previewState == block.getStateForPlacement(placeContext);
        int tint = valid ? PlacementPreviewSubmission.VALID_TINT : PlacementPreviewSubmission.INVALID_TINT;

        PlacementPreviewSubmission.submit(
                event.getPoseStack(), event.getSubmitNodeCollector(), player.level(),
                multiblockPreviewer, previewState, origin, facing, tint
        );
    }
}