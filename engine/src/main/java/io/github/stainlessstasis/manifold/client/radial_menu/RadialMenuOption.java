package io.github.stainlessstasis.manifold.client.radial_menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record RadialMenuOption<T>(T value, ItemStack icon, Component label) {}