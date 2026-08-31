package com.apexballistics.item;

import com.apexballistics.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

public final class LocationItems {
    private LocationItems() {
    }

    public static boolean isLocationItem(ItemStack stack) {
        return stack.getItem() instanceof TargetDesignatorItem
                || stack.getItem() instanceof CoordToolItem;
    }
}
