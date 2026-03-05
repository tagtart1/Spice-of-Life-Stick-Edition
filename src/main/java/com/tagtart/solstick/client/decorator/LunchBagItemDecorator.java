package com.tagtart.solstick.client.decorator;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class LunchBagItemDecorator implements IItemDecorator {
    private static final LunchBagItemDecorator INSTANCE = new LunchBagItemDecorator();
    private static final int OVERLAY_OFFSET_X = 10;
    private static final int OVERLAY_OFFSET_Y = 8;

    private LunchBagItemDecorator() {
    }

    @SubscribeEvent
    public static void registerDecorators(RegisterItemDecorationsEvent event) {
        event.register(ModItems.LUNCH_BAG.get(), INSTANCE);
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        ItemStack selectedFood = LunchBagItem.getSelectedFoodStack(stack, Minecraft.getInstance().player);
        if (selectedFood.isEmpty()) {
            return false;
        }

        // Draw selected-food preview in the item icon's bottom-right corner.
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(xOffset + OVERLAY_OFFSET_X, yOffset + OVERLAY_OFFSET_Y, 136.0D);
        guiGraphics.pose().scale(0.4F, 0.4F, 1.0F);
        guiGraphics.renderFakeItem(selectedFood, 0, 0);
        guiGraphics.pose().popPose();
        return false;
    }
}
