package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.pedroricardo.commander.content.CommanderCommandSource;

@SuppressWarnings("unchecked")
public class SeedCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("seed")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                    source.sendTranslatableMessage("commands.commander.seed.success", source.getWorld().getRandomSeed());
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
