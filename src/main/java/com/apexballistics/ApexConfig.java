package com.apexballistics;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ApexBallistics.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ApexConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue GRIEFING = BUILDER
            .comment("If true, missile explosions break blocks (still respects the explosion interaction type).")
            .define("griefing", true);

    private static final ForgeConfigSpec.DoubleValue POWER_MULTIPLIER = BUILDER
            .comment("Global multiplier applied to every warhead blast radius.")
            .defineInRange("powerMultiplier", 1.0D, 0.1D, 4.0D);

    private static final ForgeConfigSpec.IntValue LAUNCHER_COOLDOWN = BUILDER
            .comment("Ticks between handheld launcher shots.")
            .defineInRange("launcherCooldownTicks", 25, 1, 200);

    private static final ForgeConfigSpec.IntValue MAX_LIFETIME = BUILDER
            .comment("Ticks a missile can fly before it airbursts.")
            .defineInRange("maxLifetimeTicks", 400, 20, 1200);

    private static final ForgeConfigSpec.DoubleValue HOMING_RANGE = BUILDER
            .comment("How far homing missiles search for a living target, in blocks.")
            .defineInRange("homingRange", 32.0D, 8.0D, 96.0D);

    private static final ForgeConfigSpec.DoubleValue CRUISE_POWER = BUILDER
            .comment("Blast radius of a cruise missile. Several overlapping blasts are used so it can punch deep.")
            .defineInRange("cruiseExplosionPower", 16.0D, 4.0D, 32.0D);

    private static final ForgeConfigSpec.IntValue CRUISE_LIFETIME = BUILDER
            .comment("Ticks a cruise missile can fly before it airbursts.")
            .defineInRange("cruiseMaxLifetimeTicks", 1200, 40, 2400);

    private static final ForgeConfigSpec.DoubleValue CRUISE_ALTITUDE = BUILDER
            .comment("How many blocks above the higher of launcher/target the missile climbs before flying over.")
            .defineInRange("cruiseAltitudeBonus", 96.0D, 24.0D, 240.0D);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean griefing = true;
    public static float powerMultiplier = 1.0F;
    public static int launcherCooldownTicks = 25;
    public static int maxLifetimeTicks = 400;
    public static double homingRange = 32.0D;
    public static float cruiseExplosionPower = 16.0F;
    public static int cruiseMaxLifetimeTicks = 1200;
    public static double cruiseAltitudeBonus = 96.0D;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        griefing = GRIEFING.get();
        powerMultiplier = POWER_MULTIPLIER.get().floatValue();
        launcherCooldownTicks = LAUNCHER_COOLDOWN.get();
        maxLifetimeTicks = MAX_LIFETIME.get();
        homingRange = HOMING_RANGE.get();
        cruiseExplosionPower = CRUISE_POWER.get().floatValue();
        cruiseMaxLifetimeTicks = CRUISE_LIFETIME.get();
        cruiseAltitudeBonus = CRUISE_ALTITUDE.get();
    }
}
