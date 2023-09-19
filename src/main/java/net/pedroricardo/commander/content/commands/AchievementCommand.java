package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.achievement.AchievementList;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.AchievementArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class AchievementCommand {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HAS_ACHIEVEMENT = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.achievement.grant.exception_already_has_achievement"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("achievement"))
                .requires(c -> ((CommanderCommandSource)c).hasAdmin())
                .then(((LiteralArgumentBuilder)LiteralArgumentBuilder.<CommanderCommandSource>literal("grant")
                ).then((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("entities", EntityArgumentType.players())
                        .then(RequiredArgumentBuilder.argument("achievement", AchievementArgumentType.achievement())
                                .executes(c -> {
                                    List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get((CommanderCommandSource) c.getSource());
                                    Achievement achievement = c.getArgument("achievement", Achievement.class);

                                    if (entities.size() == 1 && ((EntityPlayer)entities.get(0)).getStat(achievement) == 1) {
                                        throw PLAYER_ALREADY_HAS_ACHIEVEMENT.create();
                                    }
                                    if (entities.size() == 0) {
                                        throw CommanderExceptions.emptySelector().create();
                                    }

                                    List<Achievement> achievements = new ArrayList<>();
                                    achievements.add(achievement);

                                    while (achievements.get(achievements.size() - 1).parent != null) {
                                        achievements.add(achievements.get(achievements.size() - 1).parent);
                                    }
                                    for (int i = 0; i < achievements.size(); i++) {
                                        for (Entity entity : entities) {
                                            ((EntityPlayer)entity).triggerAchievement(achievements.get(achievements.size() - 1 - i));
                                        }
                                    }

                                    sendContextualMessage((CommanderCommandSource) c.getSource(), entities, achievement);

                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                }))
                        .then((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("all")
                                .executes(c -> {
                                    List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get((CommanderCommandSource) c.getSource());

                                    if (entities.size() == 0) {
                                        throw CommanderExceptions.emptySelector().create();
                                    }

                                    for (Achievement achievement : AchievementList.achievementList) {
                                        List<Achievement> achievements = new ArrayList<>();
                                        achievements.add(achievement);
                                        while (achievements.get(achievements.size() - 1).parent != null) {
                                            achievements.add(achievements.get(achievements.size() - 1).parent);
                                        }
                                        for (int i = 0; i < achievements.size(); i++) {
                                            for (Entity entity : entities) {
                                                ((EntityPlayer)entity).triggerAchievement(achievements.get(achievements.size() - 1 - i));
                                            }
                                        }
                                    }
                                    return CommanderCommandManager.SINGLE_SUCCESS;
                                }))))));
    }

    private static void sendContextualMessage(CommanderCommandSource source, List<? extends Entity> entities, Achievement achievement) {
        if (entities.size() > 1) {
            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.achievement.grant.success_multiple_entities", achievement.getStatName(), entities.size()));
        } else if (entities.size() == 1) {
            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.achievement.grant.success_single_entity", achievement.getStatName().trim(), ((EntityLiving)entities.get(0)).getDisplayName()));
        } else {
            source.sendMessage(I18n.getInstance().translateKey("commands.commander.achievement.grant.failure_empty_selector"));
        }
    }
}
