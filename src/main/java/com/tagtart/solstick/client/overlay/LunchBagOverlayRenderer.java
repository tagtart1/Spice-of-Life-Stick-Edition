package com.tagtart.solstick.client.overlay;

import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.client.state.LunchBagOverlayState;
import com.tagtart.solstick.components.ModDataComponents;
import com.tagtart.solstick.item.ModItems;
import com.tagtart.solstick.item.custom.LunchBagConstants;
import com.tagtart.solstick.item.custom.LunchBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
            "textures/gui/seven_item_bar.png");
    private static final ResourceLocation SELECTION_FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/selectable_item_overlay.png");
    private static final ResourceLocation BEST_SELECTION_FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/best_selectable_item_overlay.png");
    private static final int TEXTURE_WIDTH = 142;
    private static final int TEXTURE_HEIGHT = 22;
    private static final int VERTICAL_CENTER_OFFSET = -28;
    private static final int SELECTION_FRAME_WIDTH = 24;
    private static final int SELECTION_FRAME_HEIGHT = 24;
    private static final int BEST_SELECTION_FRAME_WIDTH = 24;
    private static final int BEST_SELECTION_FRAME_HEIGHT = 24;
    private static final int ITEM_NAME_VERTICAL_GAP = 6;
    private static final int FOOD_PREVIEW_VERTICAL_GAP = 4;
    private static final int TEXT_SCREEN_PADDING = 2;
    private static final int BEST_FOOD_FIRST_TEXT_COLOR = 0x55FF55;
    private static final String BEST_FOOD_FIRST_LABEL = "Best Food First";

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
        renderSelectionFrame(event, lunchBag, x, y, minecraft);
        LunchBagItem.FoodPreview foodPreview = LunchBagItem.getSelectedFoodPreview(lunchBag, minecraft.player);
        renderFoodPreview(event, foodPreview, x, y);
        renderSelectedItemName(event, lunchBag, foodPreview, x, y, minecraft);
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
        NonNullList<ItemStack> storedItems = NonNullList.withSize(LunchBagConstants.SLOT_COUNT, ItemStack.EMPTY);
        contents.copyInto(storedItems);

        for (int i = 0; i < LunchBagConstants.SLOT_COUNT; i++) {
            ItemStack stack = storedItems.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            int slotX = left + LunchBagConstants.SLOT_X_OFFSET + i * LunchBagConstants.SLOT_STRIDE;
            int slotY = top + LunchBagConstants.SLOT_Y_OFFSET;
            event.getGuiGraphics().renderItem(stack, slotX, slotY);
            event.getGuiGraphics().renderItemDecorations(minecraft.font, stack, slotX, slotY);
        }
    }

    private static void renderSelectionFrame(RenderGuiEvent.Post event, ItemStack lunchBag, int left, int top,
            Minecraft minecraft) {
        int selected = LunchBagItem.getSelectedSlotIndex(lunchBag);
        if (LunchBagItem.isHiddenBestSlot(selected)) {
            int bestSlot = LunchBagItem.getBestFoodSlotIndex(lunchBag, minecraft.player);
            if (bestSlot < 0) {
                return;
            }

            int slotX = left + LunchBagConstants.SLOT_X_OFFSET + bestSlot * LunchBagConstants.SLOT_STRIDE;
            int slotY = top + LunchBagConstants.SLOT_Y_OFFSET;
            int frameX = slotX - (BEST_SELECTION_FRAME_WIDTH - 16) / 2;
            int frameY = slotY - (BEST_SELECTION_FRAME_HEIGHT - 16) / 2;
            event.getGuiGraphics().blit(
                    BEST_SELECTION_FRAME_TEXTURE,
                    frameX,
                    frameY,
                    0,
                    0.0F,
                    0.0F,
                    BEST_SELECTION_FRAME_WIDTH,
                    BEST_SELECTION_FRAME_HEIGHT,
                    BEST_SELECTION_FRAME_WIDTH,
                    BEST_SELECTION_FRAME_HEIGHT);
            return;
        }

        int slotX = left + LunchBagConstants.SLOT_X_OFFSET + selected * LunchBagConstants.SLOT_STRIDE;
        int slotY = top + LunchBagConstants.SLOT_Y_OFFSET;
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

    private static void renderFoodPreview(RenderGuiEvent.Post event, LunchBagItem.FoodPreview foodPreview, int left,
            int top) {
        if (foodPreview == null) {
            return;
        }

        int previewHeight = LunchBagFoodPreviewRenderer.getHeight(foodPreview);
        int previewY = Math.max(TEXT_SCREEN_PADDING, top - FOOD_PREVIEW_VERTICAL_GAP - previewHeight);
        LunchBagFoodPreviewRenderer.render(event.getGuiGraphics(), foodPreview, left, previewY, TEXTURE_WIDTH);
    }

    private static void renderSelectedItemName(RenderGuiEvent.Post event, ItemStack lunchBag,
            LunchBagItem.FoodPreview foodPreview, int left, int top, Minecraft minecraft) {
        int previewHeight = LunchBagFoodPreviewRenderer.getHeight(foodPreview);
        int nameY = top - FOOD_PREVIEW_VERTICAL_GAP - previewHeight - ITEM_NAME_VERTICAL_GAP - minecraft.font.lineHeight;

        int selectedSlot = LunchBagItem.getSelectedSlotIndex(lunchBag);
        if (LunchBagItem.isHiddenBestSlot(selectedSlot)) {
            drawCenteredText(event, minecraft, left, nameY, BEST_FOOD_FIRST_LABEL, BEST_FOOD_FIRST_TEXT_COLOR);
            return;
        }

        if (foodPreview == null) {
            return;
        }

        Component name = foodPreview.stack().getHoverName();
        int textColor = resolveItemNameColor(foodPreview.stack(), name);
        drawCenteredText(event, minecraft, left, nameY, name.getString(), textColor);
    }

    private static int resolveItemNameColor(ItemStack stack, Component name) {
        if (name.getStyle().getColor() != null) {
            return name.getStyle().getColor().getValue();
        }

        Integer rarityColor = stack.getRarity().color().getColor();
        if (rarityColor != null) {
            return rarityColor;
        }

        return 0xFFFFFF;
    }

    private static void drawCenteredText(RenderGuiEvent.Post event, Minecraft minecraft, int left, int y, String text,
            int color) {
        int textWidth = minecraft.font.width(text);
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int centeredX = left + (TEXTURE_WIDTH - textWidth) / 2;
        int maxX = Math.max(TEXT_SCREEN_PADDING, screenWidth - textWidth - TEXT_SCREEN_PADDING);
        int x = Mth.clamp(centeredX, TEXT_SCREEN_PADDING, maxX);
        y = Math.max(TEXT_SCREEN_PADDING, y);
        event.getGuiGraphics().drawString(minecraft.font, text, x, y, color, false);
    }
}
