package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.AchievementArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.EntitySelector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class AchievementCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("achievement"))
                .then(((LiteralArgumentBuilder)LiteralArgumentBuilder.<CommanderCommandSource>literal("grant")
                ).then(RequiredArgumentBuilder.argument("achievement", AchievementArgumentType.achievement())
                        .executes(c -> {
                            List<Achievement> achievements = new ArrayList<>();
                            achievements.add(c.getArgument("achievement", Achievement.class));
                            while (achievements.get(achievements.size() - 1).parent != null) {
                                achievements.add(achievements.get(achievements.size() - 1).parent);
                            }
                            for (int i = 0; i < achievements.size(); i++) {
                                ((CommanderCommandSource)c.getSource()).getSender().triggerAchievement(achievements.get(achievements.size() - 1 - i));
                            }
                            return CommanderCommandManager.SINGLE_SUCCESS;
                        }))
                        .then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("entities", EntityArgumentType.players())
                                .then(RequiredArgumentBuilder.argument("achievement", AchievementArgumentType.achievement())
                                        .executes(c -> {
                                            List<Achievement> achievements = new ArrayList<>();
                                            achievements.add(c.getArgument("achievement", Achievement.class));
                                            while (achievements.get(achievements.size() - 1).parent != null) {
                                                achievements.add(achievements.get(achievements.size() - 1).parent);
                                            }
                                            for (int i = 0; i < achievements.size(); i++) {
                                                for (Entity entity : c.getArgument("entities", EntitySelector.class).get((CommanderCommandSource) c.getSource())) {
                                                    ((EntityPlayer)entity).triggerAchievement(achievements.get(achievements.size() - 1 - i));
                                                }
                                            }
                                            return CommanderCommandManager.SINGLE_SUCCESS;
                                        }))))));
    }
}
