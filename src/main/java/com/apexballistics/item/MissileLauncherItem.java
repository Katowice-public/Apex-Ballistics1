package com.apexballistics.item;

import com.apexballistics.ApexConfig;
import com.apexballistics.entity.MissileEntity;
import com.apexballistics.entity.WarheadType;
import com.apexballistics.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class MissileLauncherItem extends Item {
    public MissileLauncherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack launcher = player.getItemInHand(hand);
        ItemStack ammo = findAmmo(player);
        boolean creative = player.getAbilities().instabuild;

        if (ammo.isEmpty() && !creative) {
            return InteractionResultHolder.fail(launcher);
        }

        WarheadType warhead = ammo.getItem() instanceof MissileItem missileItem
                ? missileItem.getWarhead()
                : WarheadType.HE;

        if (!level.isClientSide) {
            MissileEntity missile = new MissileEntity(level, player, player.getLookAngle(), warhead);
            LivingEntity locked = pickLookTarget(player, 64.0D);
            if (locked != null) {
                missile.setLockedTarget(locked);
            }
            level.addFreshEntity(missile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.MISSILE_LAUNCH.get(), SoundSource.PLAYERS, 1.0F,
                    0.85F + player.getRandom().nextFloat() * 0.2F);

            if (!creative) {
                ammo.shrink(1);
                launcher.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            player.getCooldowns().addCooldown(this, ApexConfig.launcherCooldownTicks);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(launcher, level.isClientSide());
    }

    public static ItemStack findAmmo(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof MissileItem) {
            return offhand;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof MissileItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static LivingEntity pickLookTarget(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        Predicate<Entity> filter = entity -> entity instanceof LivingEntity living
                && living.isAlive()
                && living != player
                && !living.isSpectator();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, box, filter, range * range);
        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.apexballistics.missile_launcher.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
