package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ApexBallistics.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.apexballistics"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.MISSILE_LAUNCHER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MISSILE_LAUNCHER.get());
                        output.accept(ModItems.TARGET_DESIGNATOR.get());
                        output.accept(ModItems.LAUNCH_PAD.get());
                        output.accept(ModItems.ROCKET_FUEL.get());
                        output.accept(ModItems.HE_MISSILE.get());
                        output.accept(ModItems.INCENDIARY_MISSILE.get());
                        output.accept(ModItems.CLUSTER_MISSILE.get());
                        output.accept(ModItems.HOMING_MISSILE.get());
                        output.accept(ModItems.BUNKER_MISSILE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
