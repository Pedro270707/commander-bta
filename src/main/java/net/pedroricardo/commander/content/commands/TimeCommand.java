package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.TimeArgumentType;

public class TimeCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("time")
                .requires(CommanderCommandSource::hasAdmin)
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("query")
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("daytime")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    source.sendTranslatableMessage("commands.commander.time.query", c.getSource().getWorld().getWorldTime() % 24000L);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("gametime")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    source.sendTranslatableMessage("commands.commander.time.query", source.getWorld().getWorldTime() % Integer.MAX_VALUE);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("day")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    source.sendTranslatableMessage("commands.commander.time.query", source.getWorld().getWorldTime() / 24000L % Integer.MAX_VALUE);

                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("set")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("time", TimeArgumentType.time())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    int time = c.getArgument("time", Integer.class);
                                    setWorldTime(source, source.getWorld(), time);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("day")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    setDayTime(source, source.getWorld(), 1000);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("noon")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    setDayTime(source, source.getWorld(), 6000);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("night")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    setDayTime(source, source.getWorld(), 13000);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("midnight")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    setDayTime(source, source.getWorld(), 18000);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("add")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("time", TimeArgumentType.time())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    int time = c.getArgument("time", Integer.class);
                                    addWorldTime(source, source.getWorld(), time);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static void setDayTime(CommanderCommandSource source, World world, long time) {
        setWorldTime(source, world, world.getWorldTime() - (world.getWorldTime() % 24000L) + time);
    }

    private static void setWorldTime(CommanderCommandSource source, World world, long time) {
        world.setWorldTime(time);
        for (LevelListener listener : world.listeners) {
            listener.allChanged();
        }
        source.sendTranslatableMessage("commands.commander.time.set", time);
    }

    private static void addWorldTime(CommanderCommandSource source, World world, long time) {
        setWorldTime(source, world, world.getWorldTime() + time);
    }
}
