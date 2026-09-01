package com.apexballistics.registry;

import com.apexballistics.ApexBallistics;
import com.apexballistics.entity.MissileTier;
import com.apexballistics.entity.WarheadType;
import com.apexballistics.item.CoordToolItem;
import com.apexballistics.item.CruiseMissileItem;
import com.apexballistics.item.MissileItem;
import com.apexballistics.item.MissileLauncherItem;
import com.apexballistics.item.TargetDesignatorItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ApexBallistics.MODID);

    public static final RegistryObject<Item> ROCKET_FUEL = ITEMS.register("rocket_fuel",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> HE_MISSILE = missile("he_missile", WarheadType.HE, MissileTier.T1, Rarity.COMMON);
    public static final RegistryObject<Item> HE_MISSILE_T2 = missile("he_missile_t2", WarheadType.HE, MissileTier.T2, Rarity.COMMON);
    public static final RegistryObject<Item> HE_MISSILE_T3 = missile("he_missile_t3", WarheadType.HE, MissileTier.T3, Rarity.COMMON);

    public static final RegistryObject<Item> INCENDIARY_MISSILE = missile("incendiary_missile", WarheadType.INCENDIARY, MissileTier.T1, Rarity.COMMON);
    public static final RegistryObject<Item> INCENDIARY_MISSILE_T2 = missile("incendiary_missile_t2", WarheadType.INCENDIARY, MissileTier.T2, Rarity.COMMON);
    public static final RegistryObject<Item> INCENDIARY_MISSILE_T3 = missile("incendiary_missile_t3", WarheadType.INCENDIARY, MissileTier.T3, Rarity.COMMON);

    public static final RegistryObject<Item> CLUSTER_MISSILE = missile("cluster_missile", WarheadType.CLUSTER, MissileTier.T1, Rarity.COMMON);
    public static final RegistryObject<Item> CLUSTER_MISSILE_T2 = missile("cluster_missile_t2", WarheadType.CLUSTER, MissileTier.T2, Rarity.COMMON);
    public static final RegistryObject<Item> CLUSTER_MISSILE_T3 = missile("cluster_missile_t3", WarheadType.CLUSTER, MissileTier.T3, Rarity.COMMON);

    public static final RegistryObject<Item> HOMING_MISSILE = missile("homing_missile", WarheadType.HOMING, MissileTier.T1, Rarity.UNCOMMON);
    public static final RegistryObject<Item> HOMING_MISSILE_T2 = missile("homing_missile_t2", WarheadType.HOMING, MissileTier.T2, Rarity.UNCOMMON);
    public static final RegistryObject<Item> HOMING_MISSILE_T3 = missile("homing_missile_t3", WarheadType.HOMING, MissileTier.T3, Rarity.UNCOMMON);

    public static final RegistryObject<Item> BUNKER_MISSILE = missile("bunker_missile", WarheadType.BUNKER, MissileTier.T1, Rarity.RARE);
    public static final RegistryObject<Item> BUNKER_MISSILE_T2 = missile("bunker_missile_t2", WarheadType.BUNKER, MissileTier.T2, Rarity.RARE);
    public static final RegistryObject<Item> BUNKER_MISSILE_T3 = missile("bunker_missile_t3", WarheadType.BUNKER, MissileTier.T3, Rarity.RARE);

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
            () -> new BlockItem(ModBlocks.CRUISE_LAUNCHER.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("block.apexballistics.cruise_launcher.tooltip")
                            .withStyle(ChatFormatting.GRAY));
                }
            });

    private static RegistryObject<Item> missile(String id, WarheadType warhead, MissileTier tier, Rarity base) {
        return ITEMS.register(id, () -> new MissileItem(warhead, tier, props(tier, base)));
    }

    private static Item.Properties props(MissileTier tier, Rarity base) {
        int stacks = switch (tier) {
            case T1 -> 16;
            case T2 -> 8;
            case T3 -> 4;
        };
        Rarity rarity = switch (tier) {
            case T1 -> base;
            case T2 -> base == Rarity.COMMON ? Rarity.UNCOMMON : Rarity.RARE;
            case T3 -> base == Rarity.RARE ? Rarity.EPIC : Rarity.RARE;
        };
        return new Item.Properties().stacksTo(stacks).rarity(rarity);
    }

    private ModItems() {
    }
}
