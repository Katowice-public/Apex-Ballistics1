package com.apexballistics;

import com.apexballistics.network.ApexNetwork;
import com.apexballistics.registry.ModBlockEntities;
import com.apexballistics.registry.ModBlocks;
import com.apexballistics.registry.ModCreativeTabs;
import com.apexballistics.registry.ModDataComponents;
import com.apexballistics.registry.ModEntities;
import com.apexballistics.registry.ModItems;
import com.apexballistics.registry.ModMenus;
import com.apexballistics.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ApexBallistics.MODID)
public class ApexBallistics {
    public static final String MODID = "apexballistics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ApexBallistics(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);

        modBus.addListener(this::commonSetup);
        context.registerConfig(ModConfig.Type.COMMON, ApexConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ApexNetwork::register);
        LOGGER.info("Apex Ballistics armed for Minecraft 1.21.1 / Forge");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
