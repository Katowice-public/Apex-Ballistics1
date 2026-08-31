package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import com.apexballistics.entity.MissileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ApexBallistics.MODID);

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE = ENTITY_TYPES.register("missile",
            () -> EntityType.Builder.<MissileEntity>of(MissileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("apexballistics:missile"));

    private ModEntities() {
    }
}
