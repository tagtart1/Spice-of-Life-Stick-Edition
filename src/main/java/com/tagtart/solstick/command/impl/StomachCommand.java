package com.tagtart.solstick.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tagtart.solstick.ModAttachments;
import com.tagtart.solstick.PlayerStomach;
import com.tagtart.solstick.command.ModCommand;
import com.tagtart.solstick.helper.StomachDisplayData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
        renderQueueSection(source, stomach);
        renderEffectivenessSection(source, stomach);
        return 1;
    }

    private void renderQueueSection(CommandSourceStack source, PlayerStomach stomach) {
        source.sendSuccess(() -> Component.literal("Stomach Queue").withStyle(ChatFormatting.GOLD), false);
        if (stomach.getFoodQueueAsList().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - (empty)").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        for (StomachDisplayData.QueueEntry entry : StomachDisplayData.buildQueueEntries(stomach)) {
            final Component queueLine = buildQueueLine(entry);
            source.sendSuccess(() -> queueLine, false);
        }
    }

    private Component buildQueueLine(StomachDisplayData.QueueEntry entry) {
        Component line = Component.literal(entry.lineNumber() + ". ")
                .append(entry.displayName())
                .withStyle(ChatFormatting.YELLOW);
        if (entry.nextToExit()) {
            line = line.copy().append(Component.literal(" <-- Next to exit").withStyle(ChatFormatting.RED));
        }
        return line;
    }

    private void renderEffectivenessSection(CommandSourceStack source, PlayerStomach stomach) {
        source.sendSuccess(() -> Component.literal("Effectiveness Summary").withStyle(ChatFormatting.GOLD), false);
        if (stomach.getFoodMap().isEmpty()) {
            source.sendSuccess(() -> Component.literal(" - (empty)").withStyle(ChatFormatting.GRAY), false);
            return;
        }

        for (StomachDisplayData.EffectivenessEntry entry : StomachDisplayData.buildEffectivenessEntries(stomach)) {
            final Component summaryLine = buildEffectivenessLine(entry);
            source.sendSuccess(() -> summaryLine, false);
        }
    }

    private Component buildEffectivenessLine(StomachDisplayData.EffectivenessEntry entry) {
        return entry.displayName()
                .copy()
                .append(Component.literal(" Effectiveness: "))
                .append(Component.literal(entry.percent() + "%").withStyle(ChatFormatting.GREEN));
    }
}
