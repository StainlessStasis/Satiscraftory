package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.screen.FactoryScreen;
import io.github.stainlessstasis.satiscraftory.menu.miner.MinerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public class MinerScreen extends FactoryScreen<MinerMenu> {
    private static final int HEADER_WIDTH = 96;
    private static final int HEADER_HEIGHT = 40;
    private static final int HEADER_Y = 20;

    public MinerScreen(MinerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    protected int factorySlotCount() {
        return 1;
    }

    @Override
    protected void drawHeader(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        int headerX = x + (imageWidth - HEADER_WIDTH) / 2;
        int headerY = y + HEADER_Y;
        drawHeaderFrame(graphics, headerX, headerY, HEADER_WIDTH, HEADER_HEIGHT);

        int barX = headerX + 8;
        int barY = headerY + 8;
        int barWidth = HEADER_WIDTH - 16;
        drawProgressBar(graphics, barX, barY, barWidth);

        float progress = menu.getProgressFraction();
        String status = menu.isActive() ? String.format("Mining - %.0f%%", progress * 100)
                : menu.isPowered() ? "Idle" : "Unpowered";
        scaledCenteredText(
                graphics, font, Component.literal(status),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 4,
                HEADER_TEXT_COLOR, 0.7f
        );

        String powerText = String.format("%.1f / %.1f MW", menu.getCurrentPowerConsumptionMw(), menu.getRatedPowerConsumptionMw());
        scaledCenteredText(
                graphics, font, Component.literal(powerText),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 15,
                HEADER_TEXT_COLOR, 0.7f
        );
    }

    private String formatItemsPerMinute() {
        if (!menu.isActive()) return "0";
        long intervalTicks = menu.getProducer().getInterval();
        if (intervalTicks <= 0) return "0";
        double itemsPerMinute = 1200d / intervalTicks;
        return formatRate(itemsPerMinute);
    }

    @Override
    protected void drawFactorySlots(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        var slot = menu.slots.getFirst();
        int slotX = x + slot.x - 1;
        int slotY = y + slot.y - 1;

        drawSlotBackground(graphics, slotX, slotY);

        String rateText = formatItemsPerMinute() + "/min";
        scaledCenteredText(
                graphics, font, Component.literal(rateText),
                slotX + 9,
                slotY + 21,
                ACCENT_COLOR, 0.8f
        );
    }
}