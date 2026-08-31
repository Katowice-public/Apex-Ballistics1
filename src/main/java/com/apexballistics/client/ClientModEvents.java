package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.registry.ModBlockEntities;
import com.apexballistics.registry.ModEntities;
import com.apexballistics.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ApexBallistics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.CRUISE_LAUNCHER.get(), CruiseLauncherScreen::new));
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MissileModel.LAYER, MissileModel::createBodyLayer);
        event.registerLayerDefinition(CruiseMissileModel.LAYER, CruiseMissileModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MISSILE.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntities.CRUISE_MISSILE.get(), CruiseMissileRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD.get(), LaunchPadRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUISE_LAUNCHER.get(), CruiseLauncherRenderer::new);
    }
}
