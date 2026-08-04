package io.github.stainlessstasis.satiscraftory.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;

public class MessageUtil {
    public static void warnPlayer(BlockPlaceContext context, String translationKey, Object... args) {
        if (!context.getLevel().isClientSide() && context.getPlayer() != null) {
            context.getPlayer().sendOverlayMessage(
                    Component.translatable(translationKey, args).withStyle(ChatFormatting.RED)
            );
        }
    }
}
