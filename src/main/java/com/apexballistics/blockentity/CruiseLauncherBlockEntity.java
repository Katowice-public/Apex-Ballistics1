package com.apexballistics.blockentity;

import com.apexballistics.block.CruiseLauncherBlock;
import com.apexballistics.entity.CruiseMissileEntity;
import com.apexballistics.item.CruiseMissileItem;
import com.apexballistics.item.TargetDesignatorItem;
import com.apexballistics.menu.CruiseLauncherMenu;
import com.apexballistics.registry.ModBlockEntities;
import com.apexballistics.registry.ModDataComponents;
import com.apexballistics.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class CruiseLauncherBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_LOCATION = 1;

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_MISSILE) {
                return stack.getItem() instanceof CruiseMissileItem;
            }
            if (slot == SLOT_LOCATION) {
                return stack.getItem() instanceof TargetDesignatorItem;
            }
            return false;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_MISSILE ? 8 : 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot == SLOT_LOCATION) {
                ItemStack stack = this.getStackInSlot(SLOT_LOCATION);
                if (stack.getItem() instanceof TargetDesignatorItem) {
                    BlockPos marked = stack.get(ModDataComponents.TARGET_POS.get());
                    if (marked != null) {
                        CruiseLauncherBlockEntity.this.setTarget(marked);
                        return;
                    }
                }
            }
            CruiseLauncherBlockEntity.this.sync();
        }
    };

    private int targetX;
    private int targetY;
    private int targetZ;
    private boolean hasTarget;

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CruiseLauncherBlockEntity.this.targetX;
                case 1 -> CruiseLauncherBlockEntity.this.targetY;
                case 2 -> CruiseLauncherBlockEntity.this.targetZ;
                case 3 -> CruiseLauncherBlockEntity.this.hasTarget ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CruiseLauncherBlockEntity.this.targetX = value;
                case 1 -> CruiseLauncherBlockEntity.this.targetY = value;
                case 2 -> CruiseLauncherBlockEntity.this.targetZ = value;
                case 3 -> CruiseLauncherBlockEntity.this.hasTarget = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CruiseLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUISE_LAUNCHER.get(), pos, state);
        this.targetX = pos.getX();
        this.targetY = pos.getY();
        this.targetZ = pos.getZ();
    }

    public ItemStackHandler getItems() {
        return this.items;
    }

    public boolean hasMissile() {
        return this.items.getStackInSlot(SLOT_MISSILE).getItem() instanceof CruiseMissileItem;
    }

    public boolean hasTarget() {
        return this.hasTarget;
    }

    @Nullable
    public BlockPos getTarget() {
        return this.hasTarget ? new BlockPos(this.targetX, this.targetY, this.targetZ) : null;
    }

    public void setTarget(BlockPos target) {
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.targetZ = target.getZ();
        this.hasTarget = true;
        this.sync();
    }

    public boolean tryLaunch(@Nullable Player player) {
        if (this.level == null || this.level.isClientSide) {
            return false;
        }
        if (!this.hasMissile()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("gui.apexballistics.cruise_launcher.missing_missile"), true);
            }
            return false;
        }
        if (!this.hasTarget) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("gui.apexballistics.cruise_launcher.missing_target"), true);
            }
            return false;
        }

        Direction facing = this.getBlockState().getValue(CruiseLauncherBlock.FACING);
        Vec3 spawn = Vec3.atBottomCenterOf(this.worldPosition)
                .add(facing.getStepX() * 0.5D, 1.15D, facing.getStepZ() * 0.5D);
        CruiseMissileEntity missile = new CruiseMissileEntity(this.level, spawn.x, spawn.y, spawn.z, new Vec3(0.0D, 1.0D, 0.0D));
        if (player != null) {
            missile.setOwner(player);
        }
        missile.setCruiseTarget(new BlockPos(this.targetX, this.targetY, this.targetZ));
        this.level.addFreshEntity(missile);
        this.level.playSound(null, this.worldPosition, ModSounds.SILO_FIRE.get(), SoundSource.BLOCKS, 1.6F, 0.7F);

        if (player == null || !player.getAbilities().instabuild) {
            this.items.extractItem(SLOT_MISSILE, 1, false);
        }
        this.sync();
        return true;
    }

    public void dropContents() {
        if (this.level == null) {
            return;
        }
        for (int i = 0; i < this.items.getSlots(); i++) {
            ItemStack stack = this.items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack);
                this.items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.apexballistics.cruise_launcher");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CruiseLauncherMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", this.items.serializeNBT(registries));
        tag.putInt("TargetX", this.targetX);
        tag.putInt("TargetY", this.targetY);
        tag.putInt("TargetZ", this.targetZ);
        tag.putBoolean("HasTarget", this.hasTarget);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            this.items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        if (tag.contains("Target")) {
            BlockPos pos = BlockPos.of(tag.getLong("Target"));
            this.targetX = pos.getX();
            this.targetY = pos.getY();
            this.targetZ = pos.getZ();
            this.hasTarget = true;
        } else {
            this.targetX = tag.contains("TargetX") ? tag.getInt("TargetX") : this.worldPosition.getX();
            this.targetY = tag.contains("TargetY") ? tag.getInt("TargetY") : this.worldPosition.getY();
            this.targetZ = tag.contains("TargetZ") ? tag.getInt("TargetZ") : this.worldPosition.getZ();
            this.hasTarget = tag.getBoolean("HasTarget");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = this.worldPosition;
        Direction facing = this.getBlockState().getValue(CruiseLauncherBlock.FACING);
        BlockPos head = pos.relative(facing);
        return AABB.encapsulatingFullBlocks(pos, head.above(4));
    }
}
