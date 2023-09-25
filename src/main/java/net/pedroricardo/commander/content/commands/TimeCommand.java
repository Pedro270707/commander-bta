package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.LevelListener;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.TimeArgumentType;

@SuppressWarnings("unchecked")
public class TimeCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("time")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(LiteralArgumentBuilder.literal("query")
                        .then(LiteralArgumentBuilder.literal("daytime")
                                .executes(c -> {
                                    ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.time.query", ((CommanderCommandSource)c.getSource()).getWorld().getWorldTime() % 24000L));

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.literal("gametime")
                                .executes(c -> {
                                    ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.time.query", ((CommanderCommandSource)c.getSource()).getWorld().getWorldTime() % Integer.MAX_VALUE));

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.literal("day")
                                .executes(c -> {
                                    ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.time.query", (int) Math.floor(((CommanderCommandSource)c.getSource()).getWorld().getWorldTime() / 24000L % Integer.MAX_VALUE)));

                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.literal("set")
                        .then(RequiredArgumentBuilder.argument("time", TimeArgumentType.time())
                                .executes(c -> {
                                    int time = c.getArgument("time", Integer.class);
                                    setWorldTime((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), time);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.literal("day")
                                .executes(c -> {
                                    setDayTime((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), 1000);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.literal("noon")
                                .executes(c -> {
                                    setDayTime((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), 6000);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.literal("night")
                                .executes(c -> {
                                    setDayTime((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), 13000);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(LiteralArgumentBuilder.literal("midnight")
                                .executes(c -> {
                                    setDayTime((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), 18000);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.literal("add")
                        .then(RequiredArgumentBuilder.argument("time", TimeArgumentType.time())
                                .executes(c -> {
                                    int time = c.getArgument("time", Integer.class);
                                    addWorldTime((CommanderCommandSource)c.getSource(), ((CommanderCommandSource)c.getSource()).getWorld(), time);
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
        source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.time.set", time));
    }

    private static void addWorldTime(CommanderCommandSource source, World world, long time) {
        setWorldTime(source, world, world.getWorldTime() + time);
    }
}
