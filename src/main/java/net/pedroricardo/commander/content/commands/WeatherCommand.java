package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.TimeArgumentType;
import net.pedroricardo.commander.content.arguments.WeatherArgumentType;

public class WeatherCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("weather")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, Weather>argument("weather", WeatherArgumentType.weather())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            World world = source.getWorld();
                            Weather weather = c.getArgument("weather", Weather.class);

                            world.weatherManager.overrideWeather(weather);
                            source.sendTranslatableMessage("commands.commander.weather.success", weather.getTranslatedName());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("duration", TimeArgumentType.time())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    World world = source.getWorld();
                                    Weather weather = c.getArgument("weather", Weather.class);

                                    world.weatherManager.overrideWeather(weather, c.getArgument("duration", Integer.class));
                                    source.sendTranslatableMessage("commands.commander.weather.success", weather.getTranslatedName());
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Float>argument("power", FloatArgumentType.floatArg())
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            World world = source.getWorld();
                                            Weather weather = c.getArgument("weather", Weather.class);

                                            world.weatherManager.overrideWeather(weather, c.getArgument("duration", Integer.class), c.getArgument("power", Float.class));
                                            source.sendTranslatableMessage("commands.commander.weather.success", weather.getTranslatedName());
                                            return Command.SINGLE_SUCCESS;
                                        })))));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("w")
                .requires(CommanderCommandSource::hasAdmin)
                .redirect(command));
    }
}
