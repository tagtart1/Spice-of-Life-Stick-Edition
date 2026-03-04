package com.tagtart.solstick.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tagtart.solstick.SOLStick;
import com.tagtart.solstick.command.impl.StomachCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@EventBusSubscriber(modid = SOLStick.MODID)
public final class ModCommands {
    private static final List<ModCommand> COMMANDS = List.of(
            new StomachCommand());

    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("solstick");
        for (ModCommand command : COMMANDS) {
            root.then(command.subcommand());
        }
        event.getDispatcher().register(root);
    }
}
