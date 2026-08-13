package io.github.stainlessstasis.satiscraftory.building;

import io.github.stainlessstasis.manifold.util.MessageUtil;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionResolver;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = Satiscraftory.MODID)
public final class BuildGunEvents {
    private BuildGunEvents(){}

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getItemStack();
        if (heldItem.getItem() instanceof BuildGunItem) {
            event.setUseBlock(TriState.FALSE);
        }
    }

    @SubscribeEvent
    static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (DemolitionResolver.resolve(level, event.getPos()) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;

        DemolitionTarget target = DemolitionResolver.resolve(level, event.getPos());
        if (target == null) return;

        event.setCanceled(true); // factory buildings are never mined normally, regardless of tool

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        boolean applied = DemolitionSelectionManager.toggle(player, target.canonicalPos());
        if (!applied) {
            MessageUtil.warnPlayer(player, Satiscraftory.MODID+".build_gun.selection_full", DemolitionSelectionManager.MAX_SELECTION);
        }
    }

}
