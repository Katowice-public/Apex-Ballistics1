package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ApexBallistics.MODID);

    public static final RegistryObject<DataComponentType<BlockPos>> TARGET_POS = DATA_COMPONENTS.register("target_pos",
            () -> DataComponentType.<BlockPos>builder()
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC)
                    .build());

    public static final RegistryObject<DataComponentType<ItemStack>> LOADED_MISSILE = DATA_COMPONENTS.register(
            "loaded_missile",
            () -> DataComponentType.<ItemStack>builder()
                    .persistent(ItemStack.OPTIONAL_CODEC)
                    .networkSynchronized(ItemStack.OPTIONAL_STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }
}
