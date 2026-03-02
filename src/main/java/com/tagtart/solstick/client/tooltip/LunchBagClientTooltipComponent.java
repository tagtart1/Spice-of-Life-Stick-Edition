package com.tagtart.solstick.client.tooltip;

import com.tagtart.solstick.item.tooltip.LunchBagTooltipComponent;
import com.tagtart.solstick.item.custom.LunchBagConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public final class LunchBagClientTooltipComponent implements ClientTooltipComponent {
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int BORDER = 1;
    private static final int BACKGROUND_COLOR = 0xF0100010;
    private static final int SLOT_BORDER_COLOR = 0xFF555555;
    private static final int SLOT_FILL_COLOR = 0xFF1E1E1E;

    private final NonNullList<ItemStack> items;
    private final int selectedSlot;

    public LunchBagClientTooltipComponent(LunchBagTooltipComponent tooltip) {
        this.items = NonNullList.withSize(LunchBagConstants.SLOT_COUNT, ItemStack.EMPTY);
        tooltip.contents().copyInto(this.items);
        this.selectedSlot = Math.floorMod(tooltip.selectedSlot(), LunchBagConstants.SLOT_COUNT);
    }

    @Override
    public int getHeight() {
        return BORDER * 2 + SLOT_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return BORDER * 2 + LunchBagConstants.SLOT_COUNT * SLOT_WIDTH;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        int totalWidth = getWidth(font);
        int totalHeight = getHeight();
        guiGraphics.fill(x, y, x + totalWidth, y + totalHeight, BACKGROUND_COLOR);

        for (int i = 0; i < LunchBagConstants.SLOT_COUNT; i++) {
            int slotX = x + BORDER + i * SLOT_WIDTH;
            int slotY = y + BORDER;
            renderSlot(guiGraphics, slotX, slotY, items.get(i), i == selectedSlot, font);
        }
    }

    private static void renderSlot(GuiGraphics guiGraphics, int x, int y, ItemStack stack, boolean selected, Font font) {
        guiGraphics.fill(x, y, x + SLOT_WIDTH, y + SLOT_HEIGHT, SLOT_BORDER_COLOR);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_WIDTH - 1, y + SLOT_HEIGHT - 1, SLOT_FILL_COLOR);

        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, x + 1, y + 1);
            guiGraphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }

        if (selected) {
            AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, y + 1, 0);
        }
    }
}
