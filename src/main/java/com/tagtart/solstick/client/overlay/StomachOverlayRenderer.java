package com.tagtart.solstick.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tagtart.solstick.Config;
import com.tagtart.solstick.ModAttachments;
import com.tagtart.solstick.PlayerStomach;
import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.client.state.StomachOverlayState;
import com.tagtart.solstick.helper.StomachDisplayData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class StomachOverlayRenderer {
    // Textures
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/single_item_slot.png");
    private static final ResourceLocation FOOD_EMPTY_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_half");
    private static final ResourceLocation FOOD_FULL_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation APPLESKIN_HUNGER_OUTLINE_SPRITE = ResourceLocation.fromNamespaceAndPath(
            "appleskin",
            "tooltip_hunger_outline");
    private static final ResourceLocation APPLESKIN_ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "appleskin",
            "textures/icons.png");

    // Main queue slot layout
    private static final int SLOT_TEXTURE_SIZE = 22;
    private static final int ITEM_PADDING = 3;
    private static final int ITEM_ICON_SIZE = 16;
    private static final int SLOTS_PER_ROW = 12;

    // Overall overlay spacing
    private static final int TEXT_PADDING = 4;
    private static final int SECTION_GAP = 6;
    private static final int ICON_TEXT_GAP = 4;
    private static final int VERTICAL_BIAS = -12;

    // Queue slot index text (matches item-count style; scaled for overlay)
    private static final float QUEUE_SLOT_COUNT_SCALE = 0.75F;

    // Compact next-to-exit and summary layout
    private static final float COMPACT_SCALE = 0.75F;
    private static final int SUMMARY_COLUMNS = 2;
    private static final int SUMMARY_COLUMN_GAP = 12;
    private static final int SUMMARY_ROW_GAP = 2;
    private static final int FOOD_VALUE_HEIGHT = 18;

    // Hunger and saturation pip layout
    private static final int HUNGER_ICON_SIZE = 9;
    private static final int SATURATION_ICON_SIZE = 7;
    private static final int HUNGER_ICON_SPACING = 9;
    private static final int SATURATION_ICON_SPACING = 7;
    private static final int FOOD_VALUE_ROW_GAP = 2;
    private static final int APPLESKIN_TEXTURE_SIZE = 256;
    private static final int SATURATION_ICON_V = 27;

    // Colors
    private static final int TEXT_COLOR = 0xFFFFFF; // White
    private static final int NEXT_TO_EXIT_COLOR = 0xFF8A80; // Light red
    private static final String TITLE_TEXT = "Stomach Queue";

    private StomachOverlayRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        if (!VanillaGuiLayers.CHAT.equals(event.getName()) || !StomachOverlayState.isVisible()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        PlayerStomach stomach = minecraft.player.getData(ModAttachments.PLAYER_STOMACH.get());
        List<StomachDisplayData.QueueEntry> queueEntries = StomachDisplayData.buildQueueEntries(stomach);
        List<StomachDisplayData.FoodValueEntry> foodValueEntries = StomachDisplayData.buildFoodValueEntries(
                stomach,
                minecraft.player);
        StomachDisplayData.QueueEntry nextToExit = findNextToExit(queueEntries);
        int maxQueueSlots = Config.STOMACH_QUEUE_SIZE.get();
        Layout layout = createLayout(minecraft.font, maxQueueSlots, nextToExit, foodValueEntries);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int left = (screenWidth - layout.width()) / 2;
        int top = Math.max(TEXT_PADDING, (screenHeight - layout.height()) / 2 + VERTICAL_BIAS);

        renderTitle(event, minecraft.font, left, top, layout.width());
        renderNextToExit(event, minecraft, nextToExit, left, top + layout.nextToExitTop(), layout.width());
        renderQueue(event, minecraft, queueEntries, maxQueueSlots, left + layout.gridLeftInset(), top + layout.queueTop());
        renderFoodValueSummary(event, minecraft, foodValueEntries, left, top + layout.summaryTop(), layout.width());
    }

    private static StomachDisplayData.QueueEntry findNextToExit(List<StomachDisplayData.QueueEntry> queueEntries) {
        for (StomachDisplayData.QueueEntry entry : queueEntries) {
            if (entry.nextToExit()) {
                return entry;
            }
        }
        return null;
    }

    private static Layout createLayout(Font font, int maxQueueSlots, StomachDisplayData.QueueEntry nextToExit,
            List<StomachDisplayData.FoodValueEntry> foodValueEntries) {
        int columns = Math.min(SLOTS_PER_ROW, Math.max(1, maxQueueSlots));
        int rows = maxQueueSlots == 0 ? 0 : (maxQueueSlots + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW;
        int gridWidth = maxQueueSlots == 0 ? 0 : columns * SLOT_TEXTURE_SIZE;
        int gridHeight = rows * SLOT_TEXTURE_SIZE;

        int titleHeight = font.lineHeight;
        int nextToExitHeight = nextToExit == null ? 0 : scaledCeil(ITEM_ICON_SIZE);
        int nextToExitWidth = nextToExit == null ? 0 : getNextToExitWidth(font, nextToExit);
        int summaryWidth = getFoodValueSummaryWidth(font, foodValueEntries);
        int summaryHeight = getFoodValueSummaryHeight(font, foodValueEntries);
        int titleWidth = font.width(TITLE_TEXT);

        int width = Math.max(Math.max(Math.max(gridWidth, nextToExitWidth), summaryWidth), titleWidth)
                + TEXT_PADDING * 2;
        int gridLeftInset = maxQueueSlots == 0 ? 0 : (width - TEXT_PADDING * 2 - gridWidth) / 2;
        int nextToExitTop = TEXT_PADDING + titleHeight + SUMMARY_ROW_GAP;
        int queueTop = nextToExitTop;
        if (nextToExit != null) {
            queueTop += nextToExitHeight + SECTION_GAP;
        }

        int summaryTop = queueTop + gridHeight;
        if (gridHeight > 0 && summaryHeight > 0) {
            summaryTop += SECTION_GAP;
        }
        int height = summaryTop + summaryHeight + TEXT_PADDING;
        return new Layout(width, height, gridLeftInset + TEXT_PADDING, nextToExitTop, queueTop, summaryTop);
    }

    private static void renderTitle(RenderGuiLayerEvent.Post event, Font font, int left, int top, int width) {
        int x = left + (width - font.width(TITLE_TEXT)) / 2;
        event.getGuiGraphics().drawString(font, TITLE_TEXT, x, top + TEXT_PADDING, TEXT_COLOR, false);
    }

    private static int getNextToExitWidth(Font font, StomachDisplayData.QueueEntry nextToExit) {
        if (nextToExit.stack().isEmpty()) {
            return scaledCeil(
                    font.width(nextToExit.displayName().copy().append(Component.literal(" is next to exit"))));
        }
        return scaledCeil(ITEM_ICON_SIZE + ICON_TEXT_GAP + font.width("is next to exit"));
    }

    private static int getFoodValueSummaryWidth(Font font, List<StomachDisplayData.FoodValueEntry> foodValueEntries) {
        if (foodValueEntries.isEmpty()) {
            return font.width(" - (empty)");
        }

        int columnWidth = 0;
        for (StomachDisplayData.FoodValueEntry entry : foodValueEntries) {
            columnWidth = Math.max(columnWidth, getFoodValueCellWidth(font, entry));
        }

        int columns = Math.min(SUMMARY_COLUMNS, foodValueEntries.size());
        return columns * columnWidth + Math.max(0, columns - 1) * SUMMARY_COLUMN_GAP;
    }

    private static int getFoodValueSummaryHeight(Font font, List<StomachDisplayData.FoodValueEntry> foodValueEntries) {
        if (foodValueEntries.isEmpty()) {
            return font.lineHeight;
        }

        int rows = (foodValueEntries.size() + SUMMARY_COLUMNS - 1) / SUMMARY_COLUMNS;
        return rows * scaledCeil(FOOD_VALUE_HEIGHT) + Math.max(0, rows - 1) * SUMMARY_ROW_GAP;
    }

    private static int getFoodValueCellWidth(Font font, StomachDisplayData.FoodValueEntry entry) {
        if (!entry.hasFoodProperties()) {
            return scaledCeil(ITEM_ICON_SIZE);
        }

        return scaledCeil(ITEM_ICON_SIZE + ICON_TEXT_GAP + Math.max(
                getRowWidth(entry.minNutrition(), entry.maxNutrition(), HUNGER_ICON_SPACING),
                shouldRenderSaturation() ? getRowWidth(entry.minSaturation(), entry.maxSaturation(),
                        SATURATION_ICON_SPACING) : 0));
    }

    private static void renderNextToExit(RenderGuiLayerEvent.Post event, Minecraft minecraft,
            StomachDisplayData.QueueEntry nextToExit, int left, int top, int width) {
        if (nextToExit == null) {
            return;
        }

        if (nextToExit.stack().isEmpty()) {
            Component text = nextToExit.displayName().copy().append(Component.literal(" is next to exit"));
            int x = left + (width - scaledCeil(minecraft.font.width(text))) / 2;
            renderScaledText(event.getGuiGraphics(), minecraft.font, text, x, top + TEXT_PADDING, NEXT_TO_EXIT_COLOR);
            return;
        }

        String text = "is next to exit";
        int contentWidth = scaledCeil(ITEM_ICON_SIZE + ICON_TEXT_GAP + minecraft.font.width(text));
        int x = left + (width - contentWidth) / 2;
        int y = top + TEXT_PADDING;
        int textY = Math.round((ITEM_ICON_SIZE - minecraft.font.lineHeight) / 2.0F);
        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(x, y, 0.0F);
        event.getGuiGraphics().pose().scale(COMPACT_SCALE, COMPACT_SCALE, 1.0F);
        event.getGuiGraphics().renderItem(nextToExit.stack(), 0, 0);
        event.getGuiGraphics().drawString(
                minecraft.font,
                text,
                ITEM_ICON_SIZE + ICON_TEXT_GAP,
                textY,
                NEXT_TO_EXIT_COLOR,
                false);
        event.getGuiGraphics().pose().popPose();
    }

    private static void renderQueue(RenderGuiLayerEvent.Post event, Minecraft minecraft,
            List<StomachDisplayData.QueueEntry> queueEntries, int maxQueueSlots, int left, int top) {
        for (int i = 0; i < maxQueueSlots; i++) {
            int row = i / SLOTS_PER_ROW;
            int column = i % SLOTS_PER_ROW;
            int x = left + column * SLOT_TEXTURE_SIZE;
            int y = top + row * SLOT_TEXTURE_SIZE;
            event.getGuiGraphics().blit(
                    SLOT_TEXTURE,
                    x,
                    y,
                    0,
                    0.0F,
                    0.0F,
                    SLOT_TEXTURE_SIZE,
                    SLOT_TEXTURE_SIZE,
                    SLOT_TEXTURE_SIZE,
                    SLOT_TEXTURE_SIZE);

            int itemX = x + ITEM_PADDING;
            int itemY = y + ITEM_PADDING;
            if (i < queueEntries.size()) {
                ItemStack stack = queueEntries.get(i).stack();
                if (!stack.isEmpty()) {
                    event.getGuiGraphics().renderItem(stack, itemX, itemY);
                }
            }

            renderQueueSlotIndex(event.getGuiGraphics(), minecraft.font, itemX, itemY, i + 1);
        }
    }

    /**
     * Same placement as vanilla item stack counts (bottom-right of the 16x16 item cell), scaled for this overlay.
     */
    private static void renderQueueSlotIndex(GuiGraphics guiGraphics, Font font, int itemX, int itemY, int oneBasedIndex) {
        String text = Integer.toString(oneBasedIndex);
        int textX = itemX + Math.round(17.0F - font.width(text) * QUEUE_SLOT_COUNT_SCALE);
        int textY = itemY + 10;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(textX, textY, 232.0F);
        guiGraphics.pose().scale(QUEUE_SLOT_COUNT_SCALE, QUEUE_SLOT_COUNT_SCALE, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, 0xFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    private static void renderFoodValueSummary(RenderGuiLayerEvent.Post event, Minecraft minecraft,
            List<StomachDisplayData.FoodValueEntry> foodValueEntries, int left, int top, int width) {
        if (foodValueEntries.isEmpty()) {
            Component emptyText = Component.literal(" - (empty)");
            int x = left + (width - minecraft.font.width(emptyText)) / 2;
            event.getGuiGraphics().drawString(minecraft.font, emptyText, x, top, TEXT_COLOR, false);
            return;
        }

        int y = top;
        int columnWidth = 0;
        for (StomachDisplayData.FoodValueEntry entry : foodValueEntries) {
            columnWidth = Math.max(columnWidth, getFoodValueCellWidth(minecraft.font, entry));
        }

        int columns = Math.min(SUMMARY_COLUMNS, foodValueEntries.size());
        int summaryWidth = columns * columnWidth + Math.max(0, columns - 1) * SUMMARY_COLUMN_GAP;
        int summaryLeft = left + (width - summaryWidth) / 2;
        int rowHeight = scaledCeil(FOOD_VALUE_HEIGHT) + SUMMARY_ROW_GAP;
        for (int i = 0; i < foodValueEntries.size(); i++) {
            int column = i % SUMMARY_COLUMNS;
            int row = i / SUMMARY_COLUMNS;
            int x = summaryLeft + column * (columnWidth + SUMMARY_COLUMN_GAP);
            y = top + row * rowHeight;
            renderFoodValueCell(event.getGuiGraphics(), minecraft.font, foodValueEntries.get(i), x, y);
        }
    }

    private static void renderFoodValueCell(GuiGraphics guiGraphics, Font font, StomachDisplayData.FoodValueEntry entry,
            int x, int y) {
        if (!entry.hasFoodProperties()) {
            renderScaledItem(guiGraphics, entry.stack(), x, y + scaledCeil((FOOD_VALUE_HEIGHT - ITEM_ICON_SIZE) / 2));
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(COMPACT_SCALE, COMPACT_SCALE, 1.0F);
        if (!entry.stack().isEmpty()) {
            guiGraphics.renderItem(entry.stack(), 0, (FOOD_VALUE_HEIGHT - ITEM_ICON_SIZE) / 2);
        }

        int pipX = ITEM_ICON_SIZE + ICON_TEXT_GAP;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        renderHungerRow(guiGraphics, entry, pipX, 0);
        if (shouldRenderSaturation()) {
            renderSaturationRow(guiGraphics, entry, pipX, HUNGER_ICON_SIZE + FOOD_VALUE_ROW_GAP);
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    private static void renderHungerRow(GuiGraphics guiGraphics, StomachDisplayData.FoodValueEntry entry, int x,
            int y) {
        renderLayeredRow(
                guiGraphics,
                x,
                y,
                getBarCount(entry.minNutrition(), entry.maxNutrition()),
                HUNGER_ICON_SPACING,
                (graphics, halfStep, pipX, pipY) -> {
                    renderHungerBackground(graphics, entry.maxNutrition(), entry.minNutrition(), halfStep, pipX, pipY);
                    renderHungerLayer(graphics, entry.maxNutrition(), halfStep, pipX, pipY, 0.25F);
                    renderHungerLayer(graphics, entry.minNutrition(), halfStep, pipX, pipY, 1.0F);
                });
    }

    private static void renderHungerBackground(GuiGraphics guiGraphics, int maxHunger, int minHunger, int halfStep,
            int x, int y) {
        FoodIcon maxIcon = getHungerIcon(maxHunger, halfStep);
        FoodIcon minIcon = getHungerIcon(minHunger, halfStep);
        guiGraphics.blitSprite(FOOD_EMPTY_TEXTURE, x, y, HUNGER_ICON_SIZE, HUNGER_ICON_SIZE);

        boolean missingPip = maxIcon != minIcon;
        if (!missingPip || !shouldRenderSaturation()) {
            return;
        }

        guiGraphics.setColor(0.62F, 0.0F, 0.0F, 0.5F);
        guiGraphics.blitSprite(APPLESKIN_HUNGER_OUTLINE_SPRITE, x, y, HUNGER_ICON_SIZE, HUNGER_ICON_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderHungerLayer(GuiGraphics guiGraphics, int hungerValue, int halfStep, int x, int y,
            float alpha) {
        FoodIcon icon = getHungerIcon(hungerValue, halfStep);
        if (icon == FoodIcon.EMPTY) {
            return;
        }

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blitSprite(getFoodTexture(icon), x, y, HUNGER_ICON_SIZE, HUNGER_ICON_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderSaturationRow(GuiGraphics guiGraphics, StomachDisplayData.FoodValueEntry entry, int x,
            int y) {
        renderLayeredRow(
                guiGraphics,
                x,
                y,
                getBarCount(entry.minSaturation(), entry.maxSaturation()),
                SATURATION_ICON_SPACING,
                (graphics, halfStep, pipX, pipY) -> {
                    renderSaturationLayer(graphics, entry.maxSaturation(), halfStep, pipX, pipY, 0.25F);
                    renderSaturationLayer(graphics, entry.minSaturation(), halfStep, pipX, pipY, 1.0F);
                });
    }

    private static void renderSaturationLayer(GuiGraphics guiGraphics, float saturationValue, int halfStep, int x,
            int y,
            float alpha) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(
                APPLESKIN_ICONS_TEXTURE,
                x,
                y,
                getSaturationU((saturationValue - halfStep) / 2.0F),
                SATURATION_ICON_V,
                SATURATION_ICON_SIZE,
                SATURATION_ICON_SIZE,
                APPLESKIN_TEXTURE_SIZE,
                APPLESKIN_TEXTURE_SIZE);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderLayeredRow(GuiGraphics guiGraphics, int x, int y, int bars, int spacing,
            PipRenderer pipRenderer) {
        int offsetX = x + (bars - 1) * spacing;
        for (int halfStep = 0; halfStep < bars * 2; halfStep += 2) {
            pipRenderer.render(guiGraphics, halfStep, offsetX, y);
            offsetX -= spacing;
        }
    }

    private static int getRowWidth(float min, float max, int spacing) {
        return getBarCount(min, max) * spacing;
    }

    private static int getBarCount(float min, float max) {
        return Math.max(1, (int) Math.ceil(Math.max(min, max) / 2.0F));
    }

    private static ResourceLocation getFoodTexture(FoodIcon icon) {
        return switch (icon) {
            case EMPTY -> FOOD_EMPTY_TEXTURE;
            case HALF -> FOOD_HALF_TEXTURE;
            case FULL -> FOOD_FULL_TEXTURE;
        };
    }

    private static FoodIcon getHungerIcon(int hungerValue, int halfStep) {
        if (hungerValue <= halfStep) {
            return FoodIcon.EMPTY;
        }
        if (hungerValue - 1 == halfStep) {
            return FoodIcon.HALF;
        }
        return FoodIcon.FULL;
    }

    private static int getSaturationU(float effectiveSaturation) {
        if (effectiveSaturation >= 1.0F) {
            return 21;
        }
        if (effectiveSaturation > 0.5F) {
            return 14;
        }
        if (effectiveSaturation > 0.25F) {
            return 7;
        }
        if (effectiveSaturation > 0.0F) {
            return 0;
        }
        return 28;
    }

    private static boolean shouldRenderSaturation() {
        return ModList.get().isLoaded("appleskin");
    }

    private static void renderScaledText(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(COMPACT_SCALE, COMPACT_SCALE, 1.0F);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        guiGraphics.pose().popPose();
    }

    private static void renderScaledItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(COMPACT_SCALE, COMPACT_SCALE, 1.0F);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.pose().popPose();
    }

    private static int scaledCeil(int value) {
        return (int) Math.ceil(value * COMPACT_SCALE);
    }

    @FunctionalInterface
    private interface PipRenderer {
        void render(GuiGraphics guiGraphics, int halfStep, int x, int y);
    }

    private enum FoodIcon {
        EMPTY,
        HALF,
        FULL
    }

    private record Layout(int width, int height, int gridLeftInset, int nextToExitTop, int queueTop, int summaryTop) {
    }
}
