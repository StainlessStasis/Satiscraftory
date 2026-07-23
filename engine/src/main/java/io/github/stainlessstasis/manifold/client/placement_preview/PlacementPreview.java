package io.github.stainlessstasis.manifold.client.placement_preview;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockPreviewRegistry;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPlacement;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPreviewer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class PlacementPreview {
    private static final int VALID_TINT = 0x8000FFFF;
    private static final int INVALID_TINT = 0x80FF0000;

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
        System.out.println("5");

        MultiblockShape shape = multiblockPreviewer.getMultiblockShape();
        BlockPos origin = placeContext.getClickedPos();
        Direction facing = previewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean valid = previewState == block.getStateForPlacement(placeContext);

        BlockEntity blockEntity =  block.newBlockEntity(BlockPos.ZERO, previewState);
        if (blockEntity == null) return;
        BlockEntityType<?> blockEntityType = blockEntity.getType();
        MultiblockRenderer<?, ?> renderer = MultiblockPreviewRegistry.get(blockEntityType);
        if (renderer == null) return;

        int tint = valid ? VALID_TINT : INVALID_TINT;
        int light = LevelRenderer.getLightCoords(player.level(), origin);

        var poseStack = event.getPoseStack();
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(origin.getX() - camPos.x, origin.getY() - camPos.y, origin.getZ() - camPos.z);
        renderer.submitPreview(poseStack, event.getSubmitNodeCollector(), facing, light, tint);
        poseStack.popPose();
    }
}