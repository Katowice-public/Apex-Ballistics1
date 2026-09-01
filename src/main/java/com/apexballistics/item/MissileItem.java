package com.apexballistics.item;

import com.apexballistics.entity.MissileTier;
import com.apexballistics.entity.WarheadType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MissileItem extends Item {
    private final WarheadType warhead;
    private final MissileTier tier;

    public MissileItem(WarheadType warhead, Properties properties) {
        this(warhead, MissileTier.T1, properties);
    }

    public MissileItem(WarheadType warhead, MissileTier tier, Properties properties) {
        super(properties);
        this.warhead = warhead;
        this.tier = tier;
    }

    public WarheadType getWarhead() {
        return warhead;
    }

    public MissileTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ChatFormatting markColor = switch (this.tier) {
            case T1 -> ChatFormatting.GRAY;
            case T2 -> ChatFormatting.YELLOW;
            case T3 -> ChatFormatting.GOLD;
        };
        tooltip.add(Component.translatable("item.apexballistics.missile.tooltip.tier", this.tier.roman())
                .withStyle(markColor));
        tooltip.add(Component.translatable("item.apexballistics.missile.tooltip." + warhead.itemId())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "item.apexballistics.missile.tooltip.stats",
                String.format("%.0f", this.warhead.explosionPower() * this.tier.powerMultiplier()),
                (int) this.tier.arcHeight()
        ).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.apexballistics.missile.tooltip.ammo")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
