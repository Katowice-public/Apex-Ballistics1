package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import com.apexballistics.entity.CruiseMissileEntity;
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
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build("apexballistics:missile"));

    public static final RegistryObject<EntityType<CruiseMissileEntity>> CRUISE_MISSILE = ENTITY_TYPES.register("cruise_missile",
            () -> EntityType.Builder.<CruiseMissileEntity>of(CruiseMissileEntity::new, MobCategory.MISC)
                    .sized(1.4F, 0.9F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .fireImmune()
                    .build("apexballistics:cruise_missile"));

    private ModEntities() {
    }
}
