package com.apexballistics.item;

import com.apexballistics.registry.ModDataComponents;
import com.apexballistics.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class TargetDesignatorItem extends Item {
    public TargetDesignatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        stack.set(ModDataComponents.TARGET_POS.get(), pos.immutable());

        Player player = context.getPlayer();
        if (player != null && !level.isClientSide) {
            player.displayClientMessage(Component.translatable(
                    "item.apexballistics.target_designator.marked",
                    pos.getX(), pos.getY(), pos.getZ()
            ), true);
            level.playSound(null, pos, ModSounds.TARGET_LOCK.get(), SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.apexballistics.target_designator.tooltip")
                .withStyle(ChatFormatting.GRAY));
        BlockPos pos = stack.get(ModDataComponents.TARGET_POS.get());
        if (pos != null) {
            tooltip.add(Component.translatable(
                    "item.apexballistics.target_designator.target",
                    pos.getX(), pos.getY(), pos.getZ()
            ).withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("item.apexballistics.target_designator.no_target")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
