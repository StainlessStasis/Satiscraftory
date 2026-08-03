package io.github.stainlessstasis.satiscraftory.item;

import io.github.stainlessstasis.manifold.factory_power.PowerLinkable;
import io.github.stainlessstasis.manifold.item.PowerLinkItem;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.Nullable;

public class CableItem extends PowerLinkItem {
    public static final double MAX_CABLE_LENGTH = 32.0;

    public CableItem(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nullable Component validateChainStart(ServerLevel level, GlobalPos pos) {
        if (!(level.getBlockEntity(pos.pos()) instanceof PowerLinkable)) {
            return Component.literal("That block can't be connected to the power grid");
        }
        return null;
    }

    @Override
    protected @Nullable Component validateLink(ServerLevel level, GlobalPos fromPos, GlobalPos toPos, ItemStack heldStack) {
        if (!fromPos.dimension().equals(toPos.dimension())) {
            return Component.literal("Can't run a cable between dimensions");
        }
        if (!(level.getBlockEntity(toPos.pos()) instanceof PowerLinkable)) {
            return Component.literal("That block can't be connected to the power grid");
        }

        double distance = Math.sqrt(fromPos.pos().distSqr(toPos.pos()));
        if (distance > MAX_CABLE_LENGTH) {
            return Component.literal(String.format("Too far away (max %.0f blocks)", MAX_CABLE_LENGTH));
        }

        return null;
    }

    @Override
    protected void onLinkCreated(UseOnContext context, GlobalPos fromPos, GlobalPos toPos) {
        Player player = context.getPlayer();
        if (player == null) return;

        //noinspection DataFlowIssue - gamemode is nullable but should not be null here
        if (!player.gameMode().isCreative()) {
            context.getItemInHand().shrink(1);
        }
    }
}