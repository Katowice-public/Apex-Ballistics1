package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import com.apexballistics.blockentity.LaunchPadBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ApexBallistics.MODID);

    public static final RegistryObject<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD = BLOCK_ENTITIES.register("launch_pad",
            () -> BlockEntityType.Builder.of(LaunchPadBlockEntity::new, ModBlocks.LAUNCH_PAD.get()).build(null));

    private ModBlockEntities() {
    }
}
