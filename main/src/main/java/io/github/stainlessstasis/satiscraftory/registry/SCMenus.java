package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.menu.container.ContainerMenu;
import io.github.stainlessstasis.manifold.menu.generator.GeneratorMenu;
import io.github.stainlessstasis.manifold.menu.machine.MachineMenu;
import io.github.stainlessstasis.satiscraftory.menu.miner.MinerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SCMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Manifold.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MinerMenu>> MINER =
            MENUS.register("miner", () -> IMenuTypeExtension.create(MinerMenu::fromNetwork));
}
