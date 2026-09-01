package com.apexballistics.menu;

import com.apexballistics.blockentity.CruiseLauncherBlockEntity;
import com.apexballistics.item.CruiseMissileItem;
import com.apexballistics.item.LocationItems;
import com.apexballistics.registry.ModBlocks;
import com.apexballistics.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class CruiseLauncherMenu extends AbstractContainerMenu {
    private final CruiseLauncherBlockEntity launcher;
    private final ContainerLevelAccess access;

    public CruiseLauncherMenu(int containerId, Inventory playerInventory, CruiseLauncherBlockEntity launcher) {
        super(ModMenus.CRUISE_LAUNCHER.get(), containerId);
        this.launcher = launcher;
        this.access = ContainerLevelAccess.create(launcher.getLevel(), launcher.getBlockPos());

        this.addSlot(new SlotItemHandler(launcher.getItems(), CruiseLauncherBlockEntity.SLOT_MISSILE, 18, 36));
        this.addSlot(new SlotItemHandler(launcher.getItems(), CruiseLauncherBlockEntity.SLOT_LOCATION, 18, 74));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 130 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 188));
        }

        this.addDataSlots(launcher.dataAccess);
    }

    public static CruiseLauncherMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        BlockEntity entity = playerInventory.player.level().getBlockEntity(pos);
        if (!(entity instanceof CruiseLauncherBlockEntity launcher)) {
            throw new IllegalStateException("Cruise launcher missing at " + pos);
        }
        return new CruiseLauncherMenu(containerId, playerInventory, launcher);
    }

    public CruiseLauncherBlockEntity getLauncher() {
        return this.launcher;
    }

    public int getTargetX() {
        return this.launcher.dataAccess.get(0);
    }

    public int getTargetY() {
        return this.launcher.dataAccess.get(1);
    }

    public int getTargetZ() {
        return this.launcher.dataAccess.get(2);
    }

    public boolean hasMissileLoaded() {
        return this.getSlot(0).hasItem();
    }

    public boolean hasTarget() {
        return this.launcher.dataAccess.get(3) != 0;
    }

    public void handleLaunch(Player player, int x, int y, int z) {
        if (!this.stillValid(player)) {
            return;
        }
        this.launcher.setTarget(new BlockPos(x, y, z));
        this.launcher.tryLaunch(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.CRUISE_LAUNCHER.get());
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
        if (index < 2) {
            if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof CruiseMissileItem) {
            if (!this.moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (LocationItems.isLocationItem(stack)) {
            if (!this.moveItemStackTo(stack, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 29) {
            if (!this.moveItemStackTo(stack, 29, 38, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 2, 29, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 1) {
            this.unloadMissile(player);
            return true;
        }
        return false;
    }

    public void unloadMissile(Player player) {
        if (!this.stillValid(player)) {
            return;
        }
        this.launcher.ejectMissile(player);
    }
}
