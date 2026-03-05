package com.tagtart.solstick.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tagtart.solstick.Config;
import com.tagtart.solstick.ModAttachments;
import com.tagtart.solstick.PlayerStomach;
import com.tagtart.solstick.command.ModCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class StomachCommand implements ModCommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> subcommand() {
        return Commands.literal("stomach")
                .executes(context -> execute(context.getSource()));
    }

    private int execute(CommandSourceStack source) {
        Player player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command can only be used by a player."));
            return 0;
        }

        PlayerStomach stomach = player.getData(ModAttachments.PLAYER_STOMACH.get());
        renderQueueSection(source, stomach.getFoodQueueAsList());
        renderEffectivenessSection(source, stomach, stomach.getFoodMap());
        return 1;
    }

    private void renderQueueSection(CommandSourceStack source, List<ResourceLocation> queue) {
        source.sendSuccess(() -> Component.literal("Stomach Queue").withStyle(ChatFormatting.GOLD), false);
        if (queue.isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - (empty)").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        // Queue is stored oldest -> newest. Render newest first for readability,
        // and mark the oldest entry as the one that will drop out next only when full.
        boolean queueIsFull = queue.size() >= Config.STOMACH_QUEUE_SIZE.get();
        for (int queueIndex = queue.size() - 1, lineNumber = 1; queueIndex >= 0; queueIndex--, lineNumber++) {
            ResourceLocation foodId = queue.get(queueIndex);
            boolean isNextToExit = queueIsFull && queueIndex == 0;
            final Component queueLine = buildQueueLine(foodId, lineNumber, isNextToExit);
            source.sendSuccess(() -> queueLine, false);
        }
    }

    private Component buildQueueLine(ResourceLocation foodId, int lineNumber, boolean isNextToExit) {
        Component line = Component.literal(lineNumber + ". ")
                .append(resolveFoodDisplayName(foodId))
                .withStyle(ChatFormatting.YELLOW);
        if (isNextToExit) {
            line = line.copy().append(Component.literal(" <-- Next to exit").withStyle(ChatFormatting.RED));
        }
        return line;
    }

    private void renderEffectivenessSection(CommandSourceStack source, PlayerStomach stomach, Map<ResourceLocation, Integer> foodMap) {
        source.sendSuccess(() -> Component.literal("Effectiveness Summary").withStyle(ChatFormatting.GOLD), false);
        if (foodMap.isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - (empty)").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        // Keep output order stable so repeated command runs are easy to compare.
        List<ResourceLocation> sortedFoods = new ArrayList<>(foodMap.keySet());
        sortedFoods.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation foodId : sortedFoods) {
            final Component summaryLine = buildEffectivenessLine(stomach, foodId);
            source.sendSuccess(() -> summaryLine, false);
        }
    }

    private Component buildEffectivenessLine(PlayerStomach stomach, ResourceLocation foodId) {
        int percent = Math.round(stomach.getFoodEffectiveness(foodId) * 100.0F);
        return resolveFoodDisplayName(foodId)
                .copy()
                .append(Component.literal(" Effectiveness: "))
                .append(Component.literal(percent + "%").withStyle(ChatFormatting.GREEN));
    }

    private static Component resolveFoodDisplayName(ResourceLocation foodId) {
        Item item = BuiltInRegistries.ITEM.get(foodId);
        if (item == null || item == BuiltInRegistries.ITEM.byId(0)) {
            return Component.literal(foodId.toString());
        }
        return item.getDescription();
    }
}
