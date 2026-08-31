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
            .defineInRange("maxLifetimeTicks", 200, 20, 1200);

    private static final ForgeConfigSpec.DoubleValue HOMING_RANGE = BUILDER
            .comment("How far homing missiles search for a living target, in blocks.")
            .defineInRange("homingRange", 32.0D, 8.0D, 96.0D);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean griefing = true;
    public static float powerMultiplier = 1.0F;
    public static int launcherCooldownTicks = 25;
    public static int maxLifetimeTicks = 200;
    public static double homingRange = 32.0D;

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
    }
}
