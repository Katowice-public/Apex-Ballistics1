package com.apexballistics.item;

import com.apexballistics.ApexConfig;
import com.apexballistics.entity.MissileEntity;
import com.apexballistics.entity.WarheadType;
import com.apexballistics.menu.MissileLauncherMenu;
import com.apexballistics.registry.ModDataComponents;
import com.apexballistics.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
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
        if (player.isShiftKeyDown()) {
            this.openGui(player, hand);
            return InteractionResultHolder.sidedSuccess(launcher, level.isClientSide());
        }

        ItemStack ammo = peekAmmo(launcher);
        if (ammo.isEmpty() || !(ammo.getItem() instanceof MissileItem missileItem)) {
            this.openGui(player, hand);
            return InteractionResultHolder.sidedSuccess(launcher, level.isClientSide());
        }

        if (!level.isClientSide) {
            WarheadType warhead = missileItem.getWarhead();
            MissileEntity missile = new MissileEntity(level, player, player.getLookAngle(), warhead);
            missile.setTier(missileItem.getTier());
            LivingEntity locked = pickLookTarget(player, 96.0D);
            if (locked != null) {
                missile.setLockedTarget(locked);
            }
            Vec3 look = player.getLookAngle();
            Vec3 horiz = new Vec3(look.x, 0.0D, look.z);
            float yaw = player.getYRot();
            if (horiz.lengthSqr() > 1.0E-6D) {
                horiz = horiz.normalize();
                yaw = (float) (Math.toDegrees(Math.atan2(-horiz.x, horiz.z)));
            }
            Vec3 impact = locked != null
                    ? locked.position()
                    : player.position().add(horiz.scale(missileItem.getTier().range()));
            missile.startArc(yaw, impact, false);
            level.addFreshEntity(missile);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.MISSILE_LAUNCH.get(), SoundSource.PLAYERS, 1.0F,
                    0.85F + player.getRandom().nextFloat() * 0.2F);

            if (!player.getAbilities().instabuild) {
                consumeAmmo(launcher);
                launcher.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            player.getCooldowns().addCooldown(this, ApexConfig.launcherCooldownTicks);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(launcher, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return this.use(context.getLevel(), player, context.getHand()).getResult();
        }
        return InteractionResult.PASS;
    }

    private void openGui(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, opener) -> new MissileLauncherMenu(id, inventory, hand),
                Component.translatable("container.apexballistics.missile_launcher")
        ), buf -> buf.writeEnum(hand));
    }

    public static ItemStack peekAmmo(ItemStack launcher) {
        ItemStack stored = launcher.get(ModDataComponents.LOADED_MISSILE.get());
        if (stored == null || stored.isEmpty() || !(stored.getItem() instanceof MissileItem)) {
            return ItemStack.EMPTY;
        }
        return stored;
    }

    public static void setAmmo(ItemStack launcher, ItemStack ammo) {
        if (ammo == null || ammo.isEmpty() || !(ammo.getItem() instanceof MissileItem)) {
            launcher.remove(ModDataComponents.LOADED_MISSILE.get());
            return;
        }
        launcher.set(ModDataComponents.LOADED_MISSILE.get(), ammo.copy());
    }

    public static void consumeAmmo(ItemStack launcher) {
        ItemStack stored = peekAmmo(launcher).copy();
        if (stored.isEmpty()) {
            return;
        }
        stored.shrink(1);
        setAmmo(launcher, stored);
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
    public boolean isBarVisible(ItemStack stack) {
        return !peekAmmo(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        ItemStack ammo = peekAmmo(stack);
        int max = Math.max(1, ammo.getMaxStackSize());
        return Math.round(13.0F * ammo.getCount() / max);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.apexballistics.missile_launcher.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.apexballistics.missile_launcher.tooltip.gui")
                .withStyle(ChatFormatting.DARK_GRAY));
        ItemStack ammo = peekAmmo(stack);
        if (ammo.isEmpty()) {
            tooltip.add(Component.translatable("item.apexballistics.missile_launcher.empty")
                    .withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable(
                    "item.apexballistics.missile_launcher.loaded",
                    ammo.getCount(),
                    ammo.getHoverName()
            ).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
