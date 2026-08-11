package io.github.stainlessstasis.satiscraftory.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.client.screen.BuildingSelectScreen;
import io.github.stainlessstasis.satiscraftory.item.BuildGunItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Satiscraftory.MODID)
public class SCKeybinds {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Satiscraftory.id("building"));

    public static final Lazy<KeyMapping> OPEN_BUILD_MENU = Lazy.of(() ->
        new KeyMapping(
            "key."+Satiscraftory.MODID+".open_build_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    ));

    @SubscribeEvent
    static void registerBindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(SCKeybinds.CATEGORY);
        event.register(SCKeybinds.OPEN_BUILD_MENU.get());
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        while (OPEN_BUILD_MENU.get().consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof BuildGunItem) {
                mc.setScreen(new BuildingSelectScreen());
            }
        }
    }

}