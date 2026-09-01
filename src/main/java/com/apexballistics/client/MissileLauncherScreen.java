package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.menu.MissileLauncherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class MissileLauncherScreen extends AbstractContainerScreen<MissileLauncherMenu> {
    private static final ResourceLocation TEXTURE = ApexBallistics.id("textures/gui/missile_launcher.png");
    private static final int LABEL_COLOR = 0x404040;

    public MissileLauncherScreen(MissileLauncherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.apexballistics.missile_launcher.unload"),
                button -> this.unload()
        ).bounds(this.leftPos + 102, this.topPos + 35, 66, 18).build());
    }

    private void unload() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        Slot hovered = this.hoveredSlot;
        if (hovered != null && hovered.index == 0 && !hovered.hasItem() && this.menu.getCarried().isEmpty()) {
            graphics.renderTooltip(this.font,
                    Component.translatable("gui.apexballistics.missile_launcher.slot_missile"),
                    mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }
}
