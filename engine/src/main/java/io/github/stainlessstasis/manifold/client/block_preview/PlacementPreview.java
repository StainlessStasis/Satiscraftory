package io.github.stainlessstasis.manifold.client.block_preview;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.awt.*;

@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class PlacementPreview {
    public static final Color VALID_COLOR = new Color(0x8000FFFF, true);
    public static final Color INVALID_COLOR = new Color(0x80FF0000, true);

    @SubscribeEvent
    public static void renderPreview(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) return;

        PreviewHeldItemSource.Resolved resolved = PreviewHeldItemSource.resolve(player);
        ItemStack held = resolved.stack();
        if (!(held.getItem() instanceof BlockItem blockItem)) return;
        if (blockItem.getBlock() instanceof BeltBlock) return;

        if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        UseOnContext useContext = new UseOnContext(player.level(), player, InteractionHand.MAIN_HAND, held, blockHit);
        BlockPlaceContext placeContext = new BlockPlaceContext(useContext);
        if (!placeContext.canPlace()) return;

        if (blockItem.getBlock() instanceof Multiblock<?> multiblock) {
            renderMultiblockPreview(event, level, multiblock, placeContext);
        } else if (resolved.fromOverride()) {
            renderPlainBlockPreview(event, level, blockItem, placeContext);
        }
    }

    private static void renderMultiblockPreview(
            SubmitCustomGeometryEvent event, Level level, Multiblock<?> multiblock, BlockPlaceContext placeContext
    ) {
        BlockState previewState = multiblock.getPreviewPlacement(placeContext);
        if (previewState == null) return;

        BlockPos origin = placeContext.getClickedPos();
        Direction facing = previewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean valid = multiblock.isMultiblockPlacementValid(placeContext, facing);
        Color tint = valid ? VALID_COLOR : INVALID_COLOR;

        MultiblockPreviewSubmission.submit(
                event.getPoseStack(), event.getSubmitNodeCollector(), level,
                multiblock, previewState, origin, facing, tint
        );
    }

    private static void renderPlainBlockPreview(
            SubmitCustomGeometryEvent event, ClientLevel level, BlockItem blockItem, BlockPlaceContext placeContext
    ) {
        BlockState previewState = blockItem.getBlock().getStateForPlacement(placeContext);
        if (previewState == null) return;

        BlockPos origin = placeContext.getClickedPos();

        BlockPreviewSubmission.submit(
                event.getPoseStack(), event.getSubmitNodeCollector(), level, previewState, origin, VALID_COLOR
        );
    }

}