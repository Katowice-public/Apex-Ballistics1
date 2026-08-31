package com.apexballistics.blockentity;

import com.apexballistics.block.LaunchPadBlock;
import com.apexballistics.entity.MissileEntity;
import com.apexballistics.entity.WarheadType;
import com.apexballistics.item.MissileItem;
import com.apexballistics.registry.ModBlockEntities;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class LaunchPadBlockEntity extends BlockEntity {
    private ItemStack missile = ItemStack.EMPTY;
    @Nullable
    private BlockPos target;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD.get(), pos, state);
    }

    public boolean hasMissile() {
        return !this.missile.isEmpty();
    }

    public ItemStack getMissile() {
        return this.missile;
    }

    @Nullable
    public WarheadType getWarhead() {
        if (this.missile.getItem() instanceof MissileItem item) {
            return item.getWarhead();
        }
        return null;
    }

    public void loadMissile(ItemStack stack, Player player) {
        if (this.hasMissile()) {
            player.displayClientMessage(Component.translatable("block.apexballistics.launch_pad.already_loaded"), true);
            return;
        }
        if (!(stack.getItem() instanceof MissileItem)) {
            return;
        }
        ItemStack loaded = stack.copyWithCount(1);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        this.missile = loaded;
        player.displayClientMessage(Component.translatable("block.apexballistics.launch_pad.loaded", loaded.getHoverName()), true);
        this.sync();
    }

    public void setTarget(BlockPos target, Player player) {
        this.target = target.immutable();
        player.displayClientMessage(Component.translatable(
                "block.apexballistics.launch_pad.target_set",
                target.getX(), target.getY(), target.getZ()
        ), true);
        this.sync();
    }

    public void ejectMissile(Player player) {
        if (!this.hasMissile()) {
            return;
        }
        ItemStack ejected = this.missile.copy();
        this.missile = ItemStack.EMPTY;
        if (!player.getInventory().add(ejected)) {
            player.drop(ejected, false);
        }
        player.displayClientMessage(Component.translatable("block.apexballistics.launch_pad.unloaded"), true);
        this.sync();
    }

    public boolean tryFire(@Nullable Player player) {
        if (this.level == null || this.level.isClientSide || !this.hasMissile()) {
            return false;
        }
        if (!(this.missile.getItem() instanceof MissileItem missileItem)) {
            this.missile = ItemStack.EMPTY;
            this.sync();
            return false;
        }

        WarheadType warhead = missileItem.getWarhead();
        Direction facing = this.getBlockState().getValue(LaunchPadBlock.FACING);
        Vec3 spawn = Vec3.atCenterOf(this.worldPosition).add(0.0D, 0.55D, 0.0D);
        MissileEntity entity = new MissileEntity(this.level, spawn.x, spawn.y, spawn.z, new Vec3(0.0D, 1.0D, 0.0D), warhead);
        if (player != null) {
            entity.setOwner(player);
        }

        BlockPos cruise = this.target != null
                ? this.target
                : this.worldPosition.relative(facing, 40).above(12);
        entity.setSiloLaunch(true, cruise);
        this.level.addFreshEntity(entity);
        this.level.playSound(null, this.worldPosition, ModSounds.SILO_FIRE.get(), SoundSource.BLOCKS, 1.2F, 0.9F);

        if (player == null || !player.getAbilities().instabuild) {
            this.missile.shrink(1);
            if (this.missile.isEmpty()) {
                this.missile = ItemStack.EMPTY;
            }
        }
        this.sync();
        return true;
    }

    public void dropContents() {
        if (this.level != null && this.hasMissile()) {
            Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.missile);
            this.missile = ItemStack.EMPTY;
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.missile.isEmpty()) {
            tag.put("Missile", this.missile.save(registries));
        }
        if (this.target != null) {
            tag.putLong("Target", this.target.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Missile")) {
            this.missile = ItemStack.parse(registries, tag.get("Missile")).orElse(ItemStack.EMPTY);
        } else {
            this.missile = ItemStack.EMPTY;
        }
        this.target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
