package com.apexballistics.menu;

import com.apexballistics.item.MissileItem;
import com.apexballistics.item.MissileLauncherItem;
import com.apexballistics.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MissileLauncherMenu extends AbstractContainerMenu {
    private final InteractionHand hand;
    private final ItemStackHandler missile;
    private boolean loading = true;

    public MissileLauncherMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenus.MISSILE_LAUNCHER.get(), containerId);
        this.hand = hand;
        this.missile = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof MissileItem;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 16;
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (!MissileLauncherMenu.this.loading) {
                    MissileLauncherMenu.this.save(playerInventory.player);
                }
            }
        };

        ItemStack loaded = MissileLauncherItem.peekAmmo(playerInventory.player.getItemInHand(hand));
        if (!loaded.isEmpty()) {
            this.missile.setStackInSlot(0, loaded.copy());
        }
        this.loading = false;

        this.addSlot(new SlotItemHandler(this.missile, 0, 80, 36));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public static MissileLauncherMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        return new MissileLauncherMenu(containerId, playerInventory, data.readEnum(InteractionHand.class));
    }

    private void save(Player player) {
        ItemStack launcher = player.getItemInHand(this.hand);
        if (launcher.getItem() instanceof MissileLauncherItem) {
            MissileLauncherItem.setAmmo(launcher, this.missile.getStackInSlot(0));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.save(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(this.hand).getItem() instanceof MissileLauncherItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return result;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof MissileItem) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 28) {
            if (!this.moveItemStackTo(stack, 28, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 1, 28, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
