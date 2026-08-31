package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import com.apexballistics.entity.WarheadType;
import com.apexballistics.item.CoordToolItem;
import com.apexballistics.item.CruiseMissileItem;
import com.apexballistics.item.MissileItem;
import com.apexballistics.item.MissileLauncherItem;
import com.apexballistics.item.TargetDesignatorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ApexBallistics.MODID);

    public static final RegistryObject<Item> ROCKET_FUEL = ITEMS.register("rocket_fuel",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> HE_MISSILE = ITEMS.register("he_missile",
            () -> new MissileItem(WarheadType.HE, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> INCENDIARY_MISSILE = ITEMS.register("incendiary_missile",
            () -> new MissileItem(WarheadType.INCENDIARY, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> CLUSTER_MISSILE = ITEMS.register("cluster_missile",
            () -> new MissileItem(WarheadType.CLUSTER, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> HOMING_MISSILE = ITEMS.register("homing_missile",
            () -> new MissileItem(WarheadType.HOMING, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BUNKER_MISSILE = ITEMS.register("bunker_missile",
            () -> new MissileItem(WarheadType.BUNKER, new Item.Properties().stacksTo(8).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MISSILE_LAUNCHER = ITEMS.register("missile_launcher",
            () -> new MissileLauncherItem(new Item.Properties().stacksTo(1).durability(250).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> TARGET_DESIGNATOR = ITEMS.register("target_designator",
            () -> new TargetDesignatorItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> COORD_TOOL = ITEMS.register("coord_tool",
            () -> new CoordToolItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> CRUISE_MISSILE = ITEMS.register("cruise_missile",
            () -> new CruiseMissileItem(new Item.Properties().stacksTo(8).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> LAUNCH_PAD = ITEMS.register("launch_pad",
            () -> new BlockItem(ModBlocks.LAUNCH_PAD.get(), new Item.Properties()));

    public static final RegistryObject<Item> CRUISE_LAUNCHER = ITEMS.register("cruise_launcher",
            () -> new BlockItem(ModBlocks.CRUISE_LAUNCHER.get(), new Item.Properties()));

    private ModItems() {
    }
}
