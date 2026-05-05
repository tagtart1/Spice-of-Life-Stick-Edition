package com.tagtart.solstick.client.overlay;

import com.tagtart.solstick.ModAttachments;
import com.tagtart.solstick.PlayerStomach;
import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.client.state.StomachOverlayState;
import com.tagtart.solstick.helper.StomachDisplayData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SOLStick.MODID, value = Dist.CLIENT)
public final class StomachOverlayRenderer {
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            SOLStick.MODID,
            "textures/gui/single_item_slot.png");
    private static final int SLOT_TEXTURE_SIZE = 18;
    private static final int ITEM_PADDING = 1;
    private static final int SLOTS_PER_ROW = 12;
    private static final int TEXT_PADDING = 4;
    private static final int SUMMARY_GAP = 8;
    private static final int VERTICAL_BIAS = -12;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int HEADER_COLOR = 0xFFD54F;
    private static final int NEXT_TO_EXIT_COLOR = 0xFF8A80;
    private static final int EFFECTIVENESS_COLOR = 0xA5D6A7;

    private StomachOverlayRenderer() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!StomachOverlayState.isVisible()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        PlayerStomach stomach = minecraft.player.getData(ModAttachments.PLAYER_STOMACH.get());
        List<StomachDisplayData.QueueEntry> queueEntries = StomachDisplayData.buildQueueEntries(stomach);
        List<StomachDisplayData.EffectivenessEntry> effectivenessEntries = StomachDisplayData.buildEffectivenessEntries(stomach);
        List<TextLine> summaryLines = buildSummaryLines(queueEntries, effectivenessEntries);
        Layout layout = createLayout(minecraft.font, queueEntries.size(), summaryLines);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int left = (screenWidth - layout.width()) / 2;
        int top = Math.max(TEXT_PADDING, (screenHeight - layout.height()) / 2 + VERTICAL_BIAS);

        renderQueue(event, minecraft, queueEntries, left + layout.gridLeftInset(), top);
        renderSummary(event, minecraft.font, summaryLines, left, top + layout.summaryTop());
    }

    private static List<TextLine> buildSummaryLines(
            List<StomachDisplayData.QueueEntry> queueEntries,
            List<StomachDisplayData.EffectivenessEntry> effectivenessEntries) {
        List<TextLine> lines = new ArrayList<>();
        if (queueEntries.isEmpty()) {
            lines.add(new TextLine(Component.literal("Stomach Queue"), HEADER_COLOR));
            lines.add(new TextLine(Component.literal(" - (empty)"), TEXT_COLOR));
        } else {
            StomachDisplayData.QueueEntry nextToExit = null;
            for (StomachDisplayData.QueueEntry entry : queueEntries) {
                if (entry.nextToExit()) {
                    nextToExit = entry;
                    break;
                }
            }

            if (nextToExit != null) {
                lines.add(new TextLine(
                        Component.literal("Next to exit: ").append(nextToExit.displayName()),
                        NEXT_TO_EXIT_COLOR));
            } else {
                lines.add(new TextLine(Component.literal("Next to exit: n/a until full"), NEXT_TO_EXIT_COLOR));
            }
        }

        lines.add(new TextLine(Component.literal("Effectiveness Summary"), HEADER_COLOR));
        if (effectivenessEntries.isEmpty()) {
            lines.add(new TextLine(Component.literal(" - (empty)"), TEXT_COLOR));
            return lines;
        }

        for (StomachDisplayData.EffectivenessEntry entry : effectivenessEntries) {
            lines.add(new TextLine(
                    entry.displayName().copy().append(Component.literal(": " + entry.percent() + "%")),
                    EFFECTIVENESS_COLOR));
        }
        return lines;
    }

    private static Layout createLayout(Font font, int queueSize, List<TextLine> summaryLines) {
        int columns = Math.min(SLOTS_PER_ROW, Math.max(1, queueSize));
        int rows = queueSize == 0 ? 0 : (queueSize + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW;
        int gridWidth = queueSize == 0 ? 0 : columns * SLOT_TEXTURE_SIZE;
        int gridHeight = rows * SLOT_TEXTURE_SIZE;

        int summaryWidth = 0;
        for (TextLine line : summaryLines) {
            summaryWidth = Math.max(summaryWidth, font.width(line.text()));
        }

        int width = Math.max(gridWidth, summaryWidth) + TEXT_PADDING * 2;
        int gridLeftInset = queueSize == 0 ? 0 : (width - TEXT_PADDING * 2 - gridWidth) / 2;
        int summaryTop = gridHeight == 0 ? 0 : gridHeight + SUMMARY_GAP;
        int height = summaryTop + (summaryLines.size() * font.lineHeight) + TEXT_PADDING * 2;
        return new Layout(width, height, gridLeftInset + TEXT_PADDING, summaryTop + TEXT_PADDING);
    }

    private static void renderQueue(RenderGuiEvent.Post event, Minecraft minecraft,
            List<StomachDisplayData.QueueEntry> queueEntries, int left, int top) {
        for (int i = 0; i < queueEntries.size(); i++) {
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

            ItemStack stack = queueEntries.get(i).stack();
            if (stack.isEmpty()) {
                continue;
            }

            int itemX = x + ITEM_PADDING;
            int itemY = y + ITEM_PADDING;
            event.getGuiGraphics().renderItem(stack, itemX, itemY);
            event.getGuiGraphics().renderItemDecorations(minecraft.font, stack, itemX, itemY);
        }
    }

    private static void renderSummary(RenderGuiEvent.Post event, Font font, List<TextLine> summaryLines, int left, int top) {
        int y = top;
        for (TextLine line : summaryLines) {
            event.getGuiGraphics().drawString(font, line.text(), left + TEXT_PADDING, y, line.color(), false);
            y += font.lineHeight;
        }
    }

    private record TextLine(Component text, int color) {
    }

    private record Layout(int width, int height, int gridLeftInset, int summaryTop) {
    }
}
