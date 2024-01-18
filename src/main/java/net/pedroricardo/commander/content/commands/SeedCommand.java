package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.pedroricardo.commander.content.CommanderCommandSource;

public class SeedCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("seed")
                .requires(CommanderCommandSource::hasAdmin)
                .executes(c -> {
                    CommanderCommandSource source = c.getSource();
                    source.sendTranslatableMessage("commands.commander.seed.success", source.getWorld().getRandomSeed());
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
