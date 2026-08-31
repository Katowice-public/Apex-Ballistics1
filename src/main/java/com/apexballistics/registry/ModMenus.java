package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import com.apexballistics.menu.CoordToolMenu;
import com.apexballistics.menu.CruiseLauncherMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ApexBallistics.MODID);

    public static final RegistryObject<MenuType<CruiseLauncherMenu>> CRUISE_LAUNCHER = MENUS.register(
            "cruise_launcher",
            () -> IForgeMenuType.create(CruiseLauncherMenu::fromNetwork)
    );

    public static final RegistryObject<MenuType<CoordToolMenu>> COORD_TOOL = MENUS.register(
            "coord_tool",
            () -> IForgeMenuType.create(CoordToolMenu::fromNetwork)
    );

    private ModMenus() {
    }
}
