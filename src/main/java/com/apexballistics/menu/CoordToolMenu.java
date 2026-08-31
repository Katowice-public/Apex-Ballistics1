package com.apexballistics.menu;

import com.apexballistics.item.CoordToolItem;
import com.apexballistics.registry.ModDataComponents;
import com.apexballistics.registry.ModMenus;
import com.apexballistics.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CoordToolMenu extends AbstractContainerMenu {
    private final InteractionHand hand;
    private final int targetX;
    private final int targetY;
    private final int targetZ;

    public CoordToolMenu(int containerId, BlockPos pos, InteractionHand hand) {
        super(ModMenus.COORD_TOOL.get(), containerId);
        this.hand = hand;
        this.targetX = pos.getX();
        this.targetY = pos.getY();
        this.targetZ = pos.getZ();
    }

    public static CoordToolMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        InteractionHand hand = data.readEnum(InteractionHand.class);
        return new CoordToolMenu(containerId, pos, hand);
    }

    public int getTargetX() {
        return this.targetX;
    }

    public int getTargetY() {
        return this.targetY;
    }

    public int getTargetZ() {
        return this.targetZ;
    }

    public void handleSave(Player player, int x, int y, int z) {
        if (!this.stillValid(player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(this.hand);
        if (!(stack.getItem() instanceof CoordToolItem)) {
            return;
        }
        stack.set(ModDataComponents.TARGET_POS.get(), new BlockPos(x, y, z));
        player.displayClientMessage(Component.translatable(
                "gui.apexballistics.coord_tool.saved", x, y, z
        ), true);
        player.level().playSound(null, player.blockPosition(), ModSounds.TARGET_LOCK.get(), SoundSource.PLAYERS, 0.6F, 1.3F);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(this.hand).getItem() instanceof CoordToolItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
