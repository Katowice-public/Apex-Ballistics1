package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ApexBallistics.MODID);

    public static final RegistryObject<SoundEvent> MISSILE_LAUNCH = register("missile_launch");
    public static final RegistryObject<SoundEvent> MISSILE_EXPLODE = register("missile_explode");
    public static final RegistryObject<SoundEvent> SILO_FIRE = register("silo_fire");
    public static final RegistryObject<SoundEvent> TARGET_LOCK = register("target_lock");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ApexBallistics.id(name)));
    }

    private ModSounds() {
    }
}
