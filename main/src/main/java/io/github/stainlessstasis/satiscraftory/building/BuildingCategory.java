package io.github.stainlessstasis.satiscraftory.building;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.network.chat.Component;

/**
 * Tabs shown in the build gun menu. Order here is the order tabs are drawn in
 */
public enum BuildingCategory {
    // TODO: special tab for space elevator and stuff when those are added
    PRODUCTION("production"),
    POWER("power"),
    LOGISTICS("logistics"),
    ORGANIZATION("organization");

    private final String translationKey;

    BuildingCategory(String path) {
        this.translationKey = Satiscraftory.MODID+"build_menu.category." + path;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}