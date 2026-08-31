package com.apexballistics.item;

import com.apexballistics.entity.WarheadType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MissileItem extends Item {
    private final WarheadType warhead;

    public MissileItem(WarheadType warhead, Properties properties) {
        super(properties);
        this.warhead = warhead;
    }

    public WarheadType getWarhead() {
        return warhead;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.apexballistics.missile.tooltip." + warhead.itemId())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.apexballistics.missile.tooltip.ammo")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
