package com.tagtart.solstick.client.overlay;

import com.tagtart.solstick.LunchBagOverlayState;
import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.menu.custom.LunchBagMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class LunchBagOverlayRenderer {
    private static final ResourceLocation LUNCH_BAG_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/lunch_bag_screen.png");
    private static final ResourceLocation SELECTION_FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/hotbar_selection.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 36;
    private static final int VERTICAL_CENTER_OFFSET = -28;
    private static final int SELECTION_FRAME_WIDTH = 24;
    private static final int SELECTION_FRAME_HEIGHT = 23;

    private LunchBagOverlayRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!LunchBagOverlayState.isVisible()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;
        ItemStack lunchBag = getHeldLunchBag(player);
        if (lunchBag.isEmpty()) {
            LunchBagOverlayState.hide();
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = (screenWidth - TEXTURE_WIDTH) / 2;
        int y = (screenHeight - TEXTURE_HEIGHT) / 2 + VERTICAL_CENTER_OFFSET;
        y = Math.max(2, y);

        event.getGuiGraphics().blit(
                LUNCH_BAG_TEXTURE,
                x,
                y,
                0,
                0.0F,
                0.0F,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
        renderStoredItems(event, lunchBag, x, y, minecraft);
        renderSelectionFrame(event, lunchBag, x, y);
        event.getGuiGraphics().setColor(1.0F, 1.0F, 1.0F, 1.0F);

    }

    private static ItemStack getHeldLunchBag(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ModItems.LUNCH_BAG.get())) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(ModItems.LUNCH_BAG.get())) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static void renderStoredItems(RenderGuiEvent.Post event, ItemStack lunchBag, int left, int top,
            Minecraft minecraft) {
        ItemContainerContents contents = lunchBag.getOrDefault(
                ModDataComponents.LUNCH_BAG_CONTENTS.get(),
                ItemContainerContents.EMPTY);
        NonNullList<ItemStack> storedItems = NonNullList.withSize(LunchBagMenu.SLOT_COUNT, ItemStack.EMPTY);
        contents.copyInto(storedItems);

        for (int i = 0; i < LunchBagMenu.SLOT_COUNT; i++) {
            ItemStack stack = storedItems.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            int slotX = left + LunchBagMenu.SLOT_X_OFFSET + i * LunchBagMenu.SLOT_STRIDE;
            int slotY = top + LunchBagMenu.SLOT_Y_OFFSET;
            event.getGuiGraphics().renderItem(stack, slotX, slotY);
            event.getGuiGraphics().renderItemDecorations(minecraft.font, stack, slotX, slotY);
        }
    }

    private static void renderSelectionFrame(RenderGuiEvent.Post event, ItemStack lunchBag, int left, int top) {
        int selected = normalizeSelectedIndex(lunchBag.getOrDefault(ModDataComponents.LUNCH_BAG_SELECTED_SLOT.get(), 0));
        int slotX = left + LunchBagMenu.SLOT_X_OFFSET + selected * LunchBagMenu.SLOT_STRIDE;
        int slotY = top + LunchBagMenu.SLOT_Y_OFFSET;
        int frameX = slotX - (SELECTION_FRAME_WIDTH - 16) / 2;
        int frameY = slotY - (SELECTION_FRAME_HEIGHT - 16) / 2;

        event.getGuiGraphics().blit(
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
    }

    private static int normalizeSelectedIndex(int index) {
        return Math.floorMod(index, LunchBagMenu.SLOT_COUNT);
    }
}
