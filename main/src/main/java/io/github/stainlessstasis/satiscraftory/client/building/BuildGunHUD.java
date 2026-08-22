package io.github.stainlessstasis.satiscraftory.client.building;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.factory_component.Laneable;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltLaneRouter;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.SatiscraftoryConfig;
import io.github.stainlessstasis.satiscraftory.client.HudColors;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.BuildingCost;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildMode;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildModeManager;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneCosts;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionResolver;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionTarget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BuildGunHUD implements GuiLayer {
    public static final String PATH = "build_gun_hud";

    private static final int TITLE_PADDING_X = 10;
    private static final int TITLE_PADDING_Y = 5;
    private static final float LABEL_SCALE = 0.7f;
    private static final int LABEL_LINE_SPACING = 2;

    private static final int ITEM_SIZE = 16;
    private static final int ITEM_BOX_PADDING_X = 5;
    private static final int ITEM_BOX_PADDING_TOP = 4;
    private static final int ITEM_BOX_PADDING_BETWEEN = 2;
    private static final int ITEM_BOX_PADDING_BOTTOM = 3;
    private static final int ITEM_BOX_SPACING = 4;

    private static final int ROW_GAP = 4;
    private static final int BOTTOM_MARGIN = 48;

    private static final int PROGRESS_BAR_WIDTH = 100;
    private static final int PROGRESS_BAR_HEIGHT = 6;

    private record ItemBox(ItemStack icon, Component amountText, int color, int width) {}

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;

        renderHoldProgressBar(graphics, centerX, screenHeight);

        Set<BlockPos> marked = DemolitionSelectionManager.clientSelection();
        if (!marked.isEmpty()) {
            Component title = Component.translatable(
                    Satiscraftory.MODID + ".build_gun.marked_for_demolition",
                    marked.size(), DemolitionSelectionManager.MAX_SELECTION
            );
            renderPanel(graphics, mc.font, screenHeight, centerX, title, buildAggregatedRefundItemBoxes(mc.font, level, marked));
            return;
        }

        DemolitionTarget hovered = resolveHoveredDemolitionTarget(mc.hitResult, level);
        if (hovered != null) {
            Component title = Component.translatable(Satiscraftory.MODID + ".build_gun.mark_for_demolition");
            renderPanel(graphics, mc.font, screenHeight, centerX, title, buildRefundItemBoxes(mc.font, level, hovered));
            return;
        }

        BlockItem selected = BuildGunItem.getSelectedBlockItemClientSide();
        BuildingCost cost = BuildGunItem.getSelectedBuildingCostClientSide();
        List<RecipeIngredient> inputs = currentPlacementCost(cost, selected);

        Component buildingTitle = Component.translatable(Satiscraftory.MODID + ".build_gun.currently_building");
        Component buildingName = buildingNameWithMode(selected);
        renderPanel(graphics, mc.font, screenHeight, centerX, buildingTitle, buildingName, buildItemBoxes(mc.font, player, inputs));

        renderSwapModePrompt(graphics, mc, selected, screenWidth, screenHeight);
    }
    
    private List<RecipeIngredient> currentPlacementCost(@Nullable BuildingCost cost, BlockItem selected) {
        if (cost == null) return List.of();

        BeltLaneRouter.LaneRoute previewedRoute = LaneRoutePreviewRenderer.currentPreview();
        if (previewedRoute != null && previewedRoute.length() > 0) {
            return LaneCosts.computeLaneCost(cost, previewedRoute.length());
        }

        if (selected.getBlock() instanceof Laneable) {
            return LaneCosts.perBlockFallbackCost(cost);
        }

        return cost.inputs();
    }

    private static String modeLabelKey(LaneBuildMode mode) {
        return switch (mode) {
            case SINGLE -> Satiscraftory.MODID + ".build_gun.mode_single";
            case LANE -> Satiscraftory.MODID + ".build_gun.mode_lane";
            case LANE_REVERSED -> Satiscraftory.MODID + ".build_gun.mode_lane_reversed";
        };
    }

    private Component buildingNameWithMode(BlockItem selected) {
        Component name = Component.translatable(selected.getDescriptionId());
        if (!(selected.getBlock() instanceof Laneable)) return name;

        Component modeLabel = Component.translatable(modeLabelKey(LaneBuildModeManager.getClientSide()));
        return Component.translatable(Satiscraftory.MODID + ".build_gun.name_with_mode", name, modeLabel);
    }

    private void renderSwapModePrompt(GuiGraphicsExtractor graphics, Minecraft mc, BlockItem selected, int screenWidth, int screenHeight) {
        if (!(selected.getBlock() instanceof Laneable)) return;

        LaneBuildMode current = LaneBuildModeManager.getClientSide();
        Component targetModeLabel = Component.translatable(modeLabelKey(current.toggled()));

        Component prompt = Component.translatable(
                Satiscraftory.MODID + ".build_gun.swap_mode_prompt",
                mc.options.keySwapOffhand.getTranslatedKeyMessage(),
                targetModeLabel
        );

        int x = screenWidth - mc.font.width(prompt) - 10;
        int y = screenHeight - 10 - mc.font.lineHeight;
        GuiRenderUtils.text(graphics, mc.font, prompt, x, y, HudColors.LABEL_COLOR);
    }

    private @Nullable DemolitionTarget resolveHoveredDemolitionTarget(HitResult hitResult, ClientLevel level) {
        if (!(hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return null;
        return DemolitionResolver.resolve(level, blockHit.getBlockPos(), LaneBuildModeManager.getClientSide().isLane());
    }

    private void renderHoldProgressBar(GuiGraphicsExtractor graphics, int centerX, int screenHeight) {
        int holdTicks = DemolitionSelectionManager.clientHoldTicks();
        if (holdTicks <= 0) return;

        float progress = Math.min(1f, holdTicks / (float) DemolitionSelectionManager.HOLD_TICKS_TO_DEMOLISH);
        int filledWidth = Math.round(PROGRESS_BAR_WIDTH * progress);

        int barX = centerX - PROGRESS_BAR_WIDTH / 2;
        int barY = screenHeight / 2 + 20;

        graphics.fill(barX, barY, barX + PROGRESS_BAR_WIDTH, barY + PROGRESS_BAR_HEIGHT, HudColors.BAR_TRACK_BG);
        graphics.fill(barX, barY, barX + filledWidth, barY + PROGRESS_BAR_HEIGHT, HudColors.NEGATIVE_COLOR);
    }

    private void renderPanel(
            GuiGraphicsExtractor graphics, Font font, int screenHeight, int centerX,
            Component title, List<ItemBox> itemBoxes
    ) {
        renderPanel(graphics, font, screenHeight, centerX, null, title, itemBoxes);
    }

    private void renderPanel(
            GuiGraphicsExtractor graphics, Font font, int screenHeight, int centerX,
            @Nullable Component label, Component name, List<ItemBox> itemBoxes
    ) {
        int itemBoxHeight = ITEM_BOX_PADDING_TOP + ITEM_SIZE + ITEM_BOX_PADDING_BETWEEN + font.lineHeight + ITEM_BOX_PADDING_BOTTOM;

        int rowBottom = screenHeight - BOTTOM_MARGIN;
        int rowTop = itemBoxes.isEmpty() ? rowBottom : rowBottom - itemBoxHeight;
        int titleBoxBottom = itemBoxes.isEmpty() ? rowBottom : rowTop - ROW_GAP;

        renderTitleBox(graphics, font, label, name, centerX, titleBoxBottom);
        renderItemRow(graphics, font, itemBoxes, centerX, rowTop, itemBoxHeight);
    }

    private void renderTitleBox(GuiGraphicsExtractor graphics, Font font, @Nullable Component label, Component name, int centerX, int boxBottom) {
        int labelWidth = label != null ? Math.round(font.width(label) * LABEL_SCALE) : 0;
        int nameWidth = font.width(name);
        int boxWidth = Math.max(labelWidth, nameWidth) + TITLE_PADDING_X * 2;

        int labelHeight = label != null ? Math.round(font.lineHeight * LABEL_SCALE) + LABEL_LINE_SPACING : 0;
        int nameHeight = font.lineHeight;
        int boxHeight = TITLE_PADDING_Y * 2 + labelHeight + nameHeight;

        int boxX = centerX - boxWidth / 2;
        int boxTop = boxBottom - boxHeight;

        graphics.fill(boxX, boxTop, boxX + boxWidth, boxBottom, HudColors.PANEL_BG);

        if (label != null) {
            GuiRenderUtils.scaledCenteredText(graphics, font, label, centerX, boxTop + TITLE_PADDING_Y, HudColors.LABEL_COLOR, LABEL_SCALE);
        }
        GuiRenderUtils.centeredText(graphics, font, name, centerX, boxTop + TITLE_PADDING_Y + labelHeight, HudColors.PRIMARY_TEXT_COLOR);
    }

    private List<ItemBox> buildItemBoxes(Font font, LocalPlayer player, List<RecipeIngredient> inputs) {
        List<ItemBox> boxes = new ArrayList<>(inputs.size());
        for (RecipeIngredient ingredient : inputs) {

            var itemOptional = BuiltInRegistries.ITEM.getOptional(ingredient.itemId());
            ItemStack iconStack = new ItemStack(itemOptional.orElse(Items.BARRIER));

            int held = BuildGunItem.countHeld(player.getInventory(), ingredient.itemId());
            boolean sufficient = held >= ingredient.amount();
            Component amountText = Component.literal(held + "/" + ingredient.amount());
            int color = sufficient ? HudColors.POSITIVE_COLOR : HudColors.NEGATIVE_COLOR;

            int textWidth = font.width(amountText);
            int width = Math.max(ITEM_SIZE, textWidth) + ITEM_BOX_PADDING_X * 2;

            boxes.add(new ItemBox(iconStack, amountText, color, width));
        }
        return boxes;
    }

    private List<ItemBox> buildRefundItemBoxes(Font font, ClientLevel level, DemolitionTarget target) {
        List<RecipeIngredient> inputs = SatiscraftoryConfig.BUILDING_COSTS.getAsBoolean()
                ? DemolitionResolver.computeRefund(level, target) : List.of();
        return refundBoxesFor(font, inputs);
    }

    /**
     * Sums recipe costs across every marked building so the HUD shows one combined refund total
     */
    private List<ItemBox> buildAggregatedRefundItemBoxes(Font font, ClientLevel level, Set<BlockPos> marked) {
        if (!SatiscraftoryConfig.BUILDING_COSTS.getAsBoolean()) return List.of();

        boolean groupAsLane = LaneBuildModeManager.getClientSide().isLane();
        Map<Identifier, Integer> totals = new LinkedHashMap<>();
        for (BlockPos pos : marked) {
            DemolitionTarget target = DemolitionResolver.resolve(level, pos, groupAsLane);
            if (target == null) continue;
            for (RecipeIngredient ingredient : DemolitionResolver.computeRefund(level, target)) {
                totals.merge(ingredient.itemId(), ingredient.amount(), Integer::sum);
            }
        }

        List<RecipeIngredient> inputs = new ArrayList<>(totals.size());
        totals.forEach((itemId, amount) -> inputs.add(new RecipeIngredient(itemId, amount)));
        return refundBoxesFor(font, inputs);
    }

    private List<ItemBox> refundBoxesFor(Font font, List<RecipeIngredient> inputs) {
        List<ItemBox> boxes = new ArrayList<>(inputs.size());
        for (RecipeIngredient ingredient : inputs) {
            int amount = SatiscraftoryConfig.scaleForDemolishRefund(ingredient.amount());
            if (amount <= 0) continue;

            var itemOptional = BuiltInRegistries.ITEM.getOptional(ingredient.itemId());
            ItemStack iconStack = new ItemStack(itemOptional.orElse(Items.BARRIER));

            Component amountText = Component.literal("+" + amount);
            int textWidth = font.width(amountText);
            int width = Math.max(ITEM_SIZE, textWidth) + ITEM_BOX_PADDING_X * 2;

            boxes.add(new ItemBox(iconStack, amountText, HudColors.POSITIVE_COLOR, width));
        }
        return boxes;
    }

    private void renderItemRow(GuiGraphicsExtractor graphics, Font font, List<ItemBox> itemBoxes, int centerX, int rowTop, int itemBoxHeight) {
        if (itemBoxes.isEmpty()) return;

        int rowWidth = itemBoxes.stream().mapToInt(ItemBox::width).sum()
                + (itemBoxes.size() - 1) * ITEM_BOX_SPACING;
        int boxX = centerX - rowWidth / 2;

        for (ItemBox box : itemBoxes) {
            int boxRight = boxX + box.width();
            graphics.fill(boxX, rowTop, boxRight, rowTop + itemBoxHeight, HudColors.ITEM_BOX_BG);

            int iconX = boxX + (box.width() - ITEM_SIZE) / 2;
            int iconY = rowTop + ITEM_BOX_PADDING_TOP;
            graphics.item(box.icon(), iconX, iconY);

            int textY = iconY + ITEM_SIZE + ITEM_BOX_PADDING_BETWEEN;
            GuiRenderUtils.centeredText(graphics, font, box.amountText(), boxX + box.width() / 2, textY, box.color());

            boxX = boxRight + ITEM_BOX_SPACING;
        }
    }
}