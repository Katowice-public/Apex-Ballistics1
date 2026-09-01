package com.apexballistics.item;

import com.apexballistics.menu.CoordToolMenu;
import com.apexballistics.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class CoordToolItem extends Item {
    public CoordToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            openGui(serverPlayer, stack, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return this.use(context.getLevel(), player, context.getHand()).getResult();
    }

    private static void openGui(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        BlockPos pos = stack.get(ModDataComponents.TARGET_POS.get());
        if (pos == null) {
            pos = player.blockPosition();
        }
        BlockPos extra = pos.immutable();
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, opener) -> new CoordToolMenu(id, extra, hand),
                Component.translatable("container.apexballistics.coord_tool")
        ), buf -> {
            buf.writeBlockPos(extra);
            buf.writeEnum(hand);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.apexballistics.coord_tool.tooltip")
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
