package com.apexballistics.client;

import com.apexballistics.ApexBallistics;
import com.apexballistics.menu.CoordToolMenu;
import com.apexballistics.network.ApexNetwork;
import com.apexballistics.network.CoordToolSavePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public class CoordToolScreen extends AbstractContainerScreen<CoordToolMenu> {
    private static final ResourceLocation TEXTURE = ApexBallistics.id("textures/gui/coord_tool.png");
    private static final int LABEL_COLOR = 0x404040;

    private EditBox xBox;
    private EditBox yBox;
    private EditBox zBox;

    public CoordToolScreen(CoordToolMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 108;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.xBox = this.makeCoordBox(this.leftPos + 24, this.topPos + 24, this.menu.getTargetX(), "gui.apexballistics.coord_tool.x");
        this.yBox = this.makeCoordBox(this.leftPos + 24, this.topPos + 44, this.menu.getTargetY(), "gui.apexballistics.coord_tool.y");
        this.zBox = this.makeCoordBox(this.leftPos + 24, this.topPos + 64, this.menu.getTargetZ(), "gui.apexballistics.coord_tool.z");
        this.addRenderableWidget(this.xBox);
        this.addRenderableWidget(this.yBox);
        this.addRenderableWidget(this.zBox);

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.apexballistics.coord_tool.save"),
                button -> this.sendSave()
        ).bounds(this.leftPos + 48, this.topPos + 84, 80, 16).build());
    }

    private EditBox makeCoordBox(int x, int y, int value, String narrationKey) {
        EditBox box = new EditBox(this.font, x, y, 144, 14, Component.translatable(narrationKey));
        box.setMaxLength(11);
        box.setBordered(true);
        box.setFilter(text -> text.isEmpty() || text.matches("-?\\d{0,10}"));
        box.setValue(Integer.toString(value));
        return box;
    }

    private void sendSave() {
        ApexNetwork.CHANNEL.send(
                new CoordToolSavePacket(
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

    @Override
    public void onClose() {
        this.sendSave();
        super.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL_COLOR, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocused() instanceof EditBox && keyCode != 256) {
            return this.getFocused().keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
