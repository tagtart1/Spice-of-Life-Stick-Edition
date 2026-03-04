package com.tagtart.solstick.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ModCommand {
    LiteralArgumentBuilder<CommandSourceStack> subcommand();
}
