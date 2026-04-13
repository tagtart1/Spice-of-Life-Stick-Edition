package com.tagtart.solstick.client.decorator;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
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
    private static final int OVERLAY_OFFSET_Y = 7;
    private static final float OVERLAY_Z_OFFSET = 50.0F;

    private LunchBagItemDecorator() {
    }

    @SubscribeEvent
    public static void registerDecorators(RegisterItemDecorationsEvent event) {
        event.register(ModItems.LUNCH_BAG.get(), INSTANCE);
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        ItemStack selectedFood = LunchBagItem.getSelectedFoodStack(stack, player);
        if (selectedFood.isEmpty()) {
            return false;
        }

        // Draw selected-food preview in the item icon's bottom-right corner.
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(xOffset + OVERLAY_OFFSET_X, yOffset + OVERLAY_OFFSET_Y, OVERLAY_Z_OFFSET);
        guiGraphics.pose().scale(0.4F, 0.4F, 1.0F);
        guiGraphics.renderFakeItem(selectedFood, 0, 0);
        guiGraphics.pose().popPose();

        // Mirror selected food cooldown using the vanilla GUI overlay render layer.
        float cooldownPercent = player.getCooldowns().getCooldownPercent(selectedFood.getItem(), 0.0F);
        if (cooldownPercent > 0.0F) {
            int top = yOffset + Mth.floor(16.0F * (1.0F - cooldownPercent));
            int bottom = top + Mth.ceil(16.0F * cooldownPercent);
            guiGraphics.fill(RenderType.guiOverlay(), xOffset, top, xOffset + 16, bottom, 0x80FFFFFF);
        }
        return false;
    }
}
