package com.tagtart.solstick.client.screen;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.menu.custom.LunchBagMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.entity.player.Inventory;

public class LunchBagScreen extends AbstractContainerScreen<LunchBagMenu> {
    private static final ResourceLocation LUNCH_BAG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/lunch_bag_screen.png");
    private static final ResourceLocation SELECTION_FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/hotbar_selection.png");
    private static final int TITLE_COLOR = 0x404040;
    private static final int SELECTION_FRAME_WIDTH = 24;
    private static final int SELECTION_FRAME_HEIGHT = 23;

    public LunchBagScreen(LunchBagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 36;
        this.titleLabelX = 6;
        this.titleLabelY = 3;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(
                LUNCH_BAG_TEXTURE,
                this.leftPos,
                this.topPos,
                0,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                this.imageWidth,
                this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int selected = this.menu.getSelectedSlotIndex();
        int slotX = this.leftPos + LunchBagMenu.SLOT_X_OFFSET + selected * LunchBagMenu.SLOT_STRIDE;
        int slotY = this.topPos + LunchBagMenu.SLOT_Y_OFFSET;
        int frameX = slotX - (SELECTION_FRAME_WIDTH - 16) / 2;
        int frameY = slotY - (SELECTION_FRAME_HEIGHT - 16) / 2;

        guiGraphics.blit(
                SELECTION_FRAME_TEXTURE,
                frameX,
                frameY,
                0,
                0.0F,
                0.0F,
                SELECTION_FRAME_WIDTH,
                SELECTION_FRAME_HEIGHT,
                SELECTION_FRAME_WIDTH,
                SELECTION_FRAME_HEIGHT);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Adjust selected slot index
        if (scrollY == 0.0D || this.minecraft == null || this.minecraft.player == null) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int current = this.menu.getSelectedSlotIndex();
        int delta = scrollY > 0.0D ? -1 : 1;
        int next = Math.floorMod(current + delta, LunchBagMenu.SLOT_COUNT);
        this.slotClicked(this.menu.getSlot(next), next, 0, ClickType.PICKUP);
        return true;
    }
}
