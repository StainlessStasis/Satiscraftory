package io.github.stainlessstasis.manifold.registry;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.menu.container.ContainerMenu;
import io.github.stainlessstasis.manifold.menu.generator.GeneratorMenu;
import io.github.stainlessstasis.manifold.menu.machine.MachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ManifoldMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Manifold.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerMenu>> CONTAINER =
            MENUS.register("container", () -> new MenuType<>(ContainerMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::fromNetwork));
    public static final DeferredHolder<MenuType<?>, MenuType<GeneratorMenu>> GENERATOR =
            MENUS.register("generator", () -> IMenuTypeExtension.create(GeneratorMenu::fromNetwork));

}