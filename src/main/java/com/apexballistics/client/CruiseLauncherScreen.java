package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.menu.CruiseLauncherMenu;
import com.apexballistics.network.ApexNetwork;
import com.apexballistics.network.CruiseLauncherFirePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.network.PacketDistributor;

public class CruiseLauncherScreen extends AbstractContainerScreen<CruiseLauncherMenu> {
    private static final ResourceLocation TEXTURE = ApexBallistics.id("textures/gui/cruise_launcher.png");
    private static final int LABEL_COLOR = 0x404040;

    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;
    private int lastSyncedX;
    private int lastSyncedY;
    private int lastSyncedZ;

    public CruiseLauncherScreen(CruiseLauncherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 212;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 118;
    }

    @Override
    protected void init() {
        super.init();
        this.lastSyncedX = this.menu.getTargetX();
        this.lastSyncedY = this.menu.getTargetY();
        this.lastSyncedZ = this.menu.getTargetZ();

        this.xBox = this.makeCoordBox(this.leftPos + 82, this.topPos + 36, this.lastSyncedX, "gui.apexballistics.cruise_launcher.x");
        this.yBox = this.makeCoordBox(this.leftPos + 82, this.topPos + 56, this.lastSyncedY, "gui.apexballistics.cruise_launcher.y");
        this.zBox = this.makeCoordBox(this.leftPos + 82, this.topPos + 74, this.lastSyncedZ, "gui.apexballistics.cruise_launcher.z");
        this.addRenderableWidget(this.xBox);
        this.addRenderableWidget(this.yBox);
        this.addRenderableWidget(this.zBox);

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.apexballistics.cruise_launcher.launch"),
                button -> this.sendLaunch()
        ).bounds(this.leftPos + 80, this.topPos + 94, 88, 16).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.apexballistics.cruise_launcher.unload"),
                button -> this.unload()
        ).bounds(this.leftPos + 8, this.topPos + 94, 68, 16).build());
    }

    private EditBox makeCoordBox(int x, int y, int value, String narrationKey) {
        EditBox box = new EditBox(this.font, x, y, 86, 14, Component.translatable(narrationKey));
        box.setMaxLength(11);
        box.setBordered(true);
        box.setFilter(text -> text.isEmpty() || text.matches("-?\\d{0,10}"));
        box.setValue(Integer.toString(value));
        return box;
    }

    private void sendLaunch() {
        ApexNetwork.CHANNEL.send(
                new CruiseLauncherFirePacket(
                        this.menu.containerId,
                        parseCoord(this.xBox, this.menu.getTargetX()),
                        parseCoord(this.yBox, this.menu.getTargetY()),
                        parseCoord(this.zBox, this.menu.getTargetZ())
                ),
                PacketDistributor.SERVER.noArg()
        );
    }

    private static int parseCoord(EditBox box, int fallback) {
        try {
            return Integer.parseInt(box.getValue().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private void unload() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        int x = this.menu.getTargetX();
        int y = this.menu.getTargetY();
        int z = this.menu.getTargetZ();
        if (x != this.lastSyncedX || y != this.lastSyncedY || z != this.lastSyncedZ) {
            this.lastSyncedX = x;
            this.lastSyncedY = y;
            this.lastSyncedZ = z;
            this.refreshIfUnfocused(this.xBox, x);
            this.refreshIfUnfocused(this.yBox, y);
            this.refreshIfUnfocused(this.zBox, z);
        }
    }

    private void refreshIfUnfocused(EditBox box, int value) {
        if (box != null && !box.isFocused()) {
            String next = Integer.toString(value);
            if (!next.equals(box.getValue())) {
                box.setValue(next);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        Slot hovered = this.hoveredSlot;
        if (hovered != null && !hovered.hasItem() && this.menu.getCarried().isEmpty()) {
            if (hovered.index == 0) {
                graphics.renderTooltip(this.font, Component.translatable("gui.apexballistics.cruise_launcher.slot_missile"), mouseX, mouseY);
            } else if (hovered.index == 1) {
                graphics.renderTooltip(this.font, Component.translatable("gui.apexballistics.cruise_launcher.slot_location"), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
        graphics.drawString(this.font, Component.translatable("gui.apexballistics.cruise_launcher.x"), 72, 39, LABEL_COLOR, false);
        graphics.drawString(this.font, Component.translatable("gui.apexballistics.cruise_launcher.y"), 72, 59, LABEL_COLOR, false);
        graphics.drawString(this.font, Component.translatable("gui.apexballistics.cruise_launcher.z"), 72, 77, LABEL_COLOR, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocused() instanceof EditBox && keyCode != 256) {
            return this.getFocused().keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
