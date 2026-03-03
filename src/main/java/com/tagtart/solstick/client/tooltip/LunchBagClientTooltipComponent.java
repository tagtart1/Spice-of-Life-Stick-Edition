package com.tagtart.solstick.client.tooltip;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.tooltip.LunchBagTooltipComponent;
import com.tagtart.solstick.item.custom.LunchBagConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LunchBagClientTooltipComponent implements ClientTooltipComponent {
    private static final int SLOT_WIDTH = 18;
    private static final int SLOT_HEIGHT = 20;
    private static final int BORDER = 1;
    private static final ResourceLocation TOOLTIP_BACKGROUND_SPRITE = ResourceLocation
            .fromNamespaceAndPath(SOLStick.MODID, "lunchbag_tooltip_background");
    private static final ResourceLocation TOOLTIP_SLOT_SPRITE = ResourceLocation
            .fromNamespaceAndPath(SOLStick.MODID, "lunchbag_tooltip_slot");

    private final NonNullList<ItemStack> items;
    private final int selectedSlot;

    public LunchBagClientTooltipComponent(LunchBagTooltipComponent tooltip) {
        this.items = NonNullList.withSize(LunchBagConstants.SLOT_COUNT, ItemStack.EMPTY);
        tooltip.contents().copyInto(this.items);
        this.selectedSlot = Math.floorMod(tooltip.selectedSlot(), LunchBagConstants.TOTAL_SELECTABLE_SLOTS);
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
        guiGraphics.blitSprite(TOOLTIP_BACKGROUND_SPRITE, x, y, totalWidth, totalHeight);

        for (int i = 0; i < LunchBagConstants.SLOT_COUNT; i++) {
            int slotX = x + BORDER + i * SLOT_WIDTH;
            int slotY = y + BORDER;
            renderSlot(guiGraphics, slotX, slotY, items.get(i),
                    selectedSlot < LunchBagConstants.SLOT_COUNT && i == selectedSlot, font);
        }
    }

    private static void renderSlot(GuiGraphics guiGraphics, int x, int y, ItemStack stack, boolean selected, Font font) {
        guiGraphics.blitSprite(TOOLTIP_SLOT_SPRITE, x, y, 0, SLOT_WIDTH, SLOT_HEIGHT);

        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, x + 1, y + 1);
            guiGraphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }

        if (selected) {
            AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, y + 1, 0);
        }
    }
}
