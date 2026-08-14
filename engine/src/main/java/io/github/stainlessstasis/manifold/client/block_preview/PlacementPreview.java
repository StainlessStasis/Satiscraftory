package io.github.stainlessstasis.manifold.client.block_preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.awt.*;

@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class PlacementPreview {
    public static final Color VALID_COLOR = new Color(0x8000FFFF, true);
    public static final Color INVALID_COLOR = new Color(0x80FF0000, true);
    private static final Identifier BOX_TEX = Identifier.withDefaultNamespace("block/white_concrete");

    @SubscribeEvent
    public static void renderPreview(SubmitCustomGeometryEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) return;

        PreviewHeldItemSource.Resolved resolved = PreviewHeldItemSource.resolve(player);
        ItemStack held = resolved.stack();
        if (!(held.getItem() instanceof BlockItem blockItem)) return;

        if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        UseOnContext useContext = new UseOnContext(player.level(), player, InteractionHand.MAIN_HAND, held, blockHit);
        BlockPlaceContext placeContext = new BlockPlaceContext(useContext);
        if (!placeContext.canPlace()) return;

        if (blockItem.getBlock() instanceof Multiblock<?> multiblock) {
            renderMultiblockPreview(event, level, multiblock, placeContext, blockHit.getBlockPos());
        } else if (resolved.fromOverride()) {
            renderPlainBlockPreview(event, level, blockItem, placeContext);
        }
    }

    private static void renderMultiblockPreview(
            SubmitCustomGeometryEvent event, Level level, Multiblock<?> multiblock, BlockPlaceContext placeContext, BlockPos hoveredPos
    ) {
        BlockState previewState = multiblock.getPreviewPlacement(placeContext);
        if (previewState == null) return;

        BlockPos origin = placeContext.getClickedPos();
        Direction facing = previewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean valid = multiblock.isMultiblockPlacementValid(placeContext, facing);

        if (!PlacementPreviewChecker.isPreviewable(level, hoveredPos, valid)) {
            return;
        }

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

        if (blockItem.getBlock() instanceof EntityBlock entityBlock) {
            BlockEntity probe = entityBlock.newBlockEntity(BlockPos.ZERO, previewState);
            if (probe != null) {
                BlockEntityPreviewRegistry.Renderer custom = BlockEntityPreviewRegistry.get(probe.getType());
                if (custom != null) {
                    custom.submitPreview(event.getPoseStack(), event.getSubmitNodeCollector(),
                            level, previewState, origin, VALID_COLOR.getRGB());
                    return;
                }
            }
        }

        if (!BlockPreviewSubmission.submit(event.getPoseStack(), event.getSubmitNodeCollector(), level, previewState, origin, VALID_COLOR)) {
            renderFallbackBox(event.getPoseStack(), event.getSubmitNodeCollector(), level, previewState, origin, VALID_COLOR);
        }
    }

    private static void renderFallbackBox(
            PoseStack poseStack, SubmitNodeCollector collector, Level level, BlockState previewState, BlockPos origin, Color color
    ) {
        AABB box = previewState.getShape(level, origin).bounds();
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(origin.getX() - camPos.x, origin.getY() - camPos.y, origin.getZ() - camPos.z);
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(AtlasIds.BLOCKS, false),
                (pose, buffer) -> emitBoxQuads(pose, buffer, box, color)
        );
        poseStack.popPose();
    }

    private static void emitBoxQuads(PoseStack.Pose pose, VertexConsumer buffer, AABB box, Color color) {
        SpriteId spriteId = new SpriteId(AtlasIds.BLOCKS, BOX_TEX);
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(spriteId);

        int a = color.getAlpha(), r = color.getRed(), g = color.getGreen() , b = color.getBlue() ;
        float u = sprite.getU0(), v = sprite.getV0();
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        emitFace(pose, buffer, u, v, r, g, b, a, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, -1);  // north
        emitFace(pose, buffer, u, v, r, g, b, a, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, 0, 0, 1);   // south
        emitFace(pose, buffer, u, v, r, g, b, a, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, -1, 0, 0);  // west
        emitFace(pose, buffer, u, v, r, g, b, a, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1, 0, 0);   // east
        emitFace(pose, buffer, u, v, r, g, b, a, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 1, 0);   // top
        emitFace(pose, buffer, u, v, r, g, b, a, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0, -1, 0);  // bottom
    }

    private static void emitFace(
            PoseStack.Pose pose, VertexConsumer buffer,
            float u, float v,
            int r, int g, int b, int a,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float x2, float y2, float z2, float x3, float y3, float z3,
            float nx, float ny, float nz
    ) {
        buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
    }
}