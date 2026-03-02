package com.tagtart.solstick.client.screen;

import com.tagtart.solstick.menu.custom.LunchBagMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LunchBagScreen extends AbstractContainerScreen<LunchBagMenu> {
    private static final int BG_COLOR = 0xFF262626;
    private static final int INNER_BG_COLOR = 0xFF1A1A1A;
    private static final int SLOT_FRAME_COLOR = 0xFF8A8A8A;
    private static final int SLOT_COLOR = 0xFF3A3A3A;

    public LunchBagScreen(LunchBagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 8 + LunchBagMenu.COLUMNS * 18 + 8;
        this.imageHeight = 18 + LunchBagMenu.ROWS * 18 + 8;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, BG_COLOR);
        guiGraphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, INNER_BG_COLOR);

        int slotStartX = x + 8;
        int slotStartY = y + 18;
        for (int row = 0; row < LunchBagMenu.ROWS; row++) {
            for (int column = 0; column < LunchBagMenu.COLUMNS; column++) {
                int slotX = slotStartX + column * 18;
                int slotY = slotStartY + row * 18;
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, SLOT_FRAME_COLOR);
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 15, slotY + 15, SLOT_COLOR);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
