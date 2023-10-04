package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.packet.Packet72UpdatePlayerProfile;
import net.minecraft.core.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class DifficultyCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("difficulty")
                .executes(c -> {
                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                    switch (source.getWorld().difficultySetting) {
                        case 0:
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.difficulty.query", I18n.getInstance().translateKey("options.difficulty.peaceful")));
                            break;
                        case 1:
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.difficulty.query", I18n.getInstance().translateKey("options.difficulty.easy")));
                            break;
                        case 2:
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.difficulty.query", I18n.getInstance().translateKey("options.difficulty.normal")));
                            break;
                        case 3:
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.difficulty.query", I18n.getInstance().translateKey("options.difficulty.hard")));
                            break;
                        default:
                            source.sendMessage(I18n.getInstance().translateKey("commands.commander.difficulty.query.unknown"));
                            break;
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.literal("peaceful")
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            return setDifficulty(source, 0);
                        }))
                .then(LiteralArgumentBuilder.literal("easy")
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            return setDifficulty(source, 1);
                        }))
                .then(LiteralArgumentBuilder.literal("normal")
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            return setDifficulty(source, 2);
                        }))
                .then(LiteralArgumentBuilder.literal("hard")
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            return setDifficulty(source, 3);
                        })));
    }

    private static final List<String> difficultyStrings = Arrays.asList("peaceful", "easy", "normal", "hard");

    private static int setDifficulty(CommanderCommandSource source, int difficulty) throws CommandSyntaxException {
        if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

        MinecraftServer server = ((IServerCommandSource)source).getServer();

        for (World world : server.worldMngr) {
            world.difficultySetting = difficulty;
        }
        server.difficulty = difficulty;
        source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.difficulty.set", I18n.getInstance().translateKey("options.difficulty." + difficultyStrings.get(difficulty))));
        return Command.SINGLE_SUCCESS;
    }
}
