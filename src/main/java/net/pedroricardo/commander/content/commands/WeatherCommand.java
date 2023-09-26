package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.World;
import net.minecraft.core.world.weather.Weather;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.TimeArgumentType;
import net.pedroricardo.commander.content.arguments.WeatherArgumentType;

@SuppressWarnings("unchecked")
public class WeatherCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<Object> command = dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("weather")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .then(RequiredArgumentBuilder.argument("weather", WeatherArgumentType.weather())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            World world = source.getWorld();
                            Weather weather = c.getArgument("weather", Weather.class);

                            world.newWeather = weather;
                            world.weatherDuration = world.getRandomWeatherDuration();
                            world.weatherIntensity = 0;
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.weather.success", weather.getTranslatedName()));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.argument("duration", TimeArgumentType.time())
                                .executes(c -> {
                                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                                    World world = source.getWorld();
                                    Weather weather = c.getArgument("weather", Weather.class);

                                    world.newWeather = weather;
                                    world.weatherDuration = c.getArgument("duration", Integer.class);
                                    world.weatherIntensity = 0;
                                    source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.weather.success", weather.getTranslatedName()));
                                    return Command.SINGLE_SUCCESS;
                                }))));
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("w")
                .redirect(command));
    }
}
