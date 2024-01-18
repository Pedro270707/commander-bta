package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.achievement.Achievement;
import net.minecraft.core.achievement.AchievementList;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.AchievementArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.ArrayList;
import java.util.List;

public class AchievementCommand {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HAS_ACHIEVEMENT = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.achievement.grant.exception_already_has_achievement"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(((LiteralArgumentBuilder.<CommanderCommandSource>literal("achievement"))
                .requires(CommanderCommandSource::hasAdmin)
                .then((LiteralArgumentBuilder.<CommanderCommandSource>literal("grant"))
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("entities", EntityArgumentType.players())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Achievement>argument("achievement", AchievementArgumentType.achievement())
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get(source);
                                            Achievement achievement = c.getArgument("achievement", Achievement.class);

                                            if (entities.size() == 1 && ((EntityPlayer)entities.get(0)).getStat(achievement) != 0) {
                                                throw PLAYER_ALREADY_HAS_ACHIEVEMENT.create();
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

                                            sendContextualMessage(source, entities, achievement);

                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("*")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get(source);

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

                                            sendWildcardContextualMessage(source, entities);

                                            return Command.SINGLE_SUCCESS;
                                }))))));
    }

    private static void sendContextualMessage(CommanderCommandSource source, List<? extends Entity> entities, Achievement achievement) {
        if (entities.size() == 1) {
            source.sendTranslatableMessage("commands.commander.achievement.grant.success_single_entity", achievement.getStatName().trim(), ((EntityLiving)entities.get(0)).getDisplayName());
        } else {
            source.sendTranslatableMessage("commands.commander.achievement.grant.success_multiple_entities", achievement.getStatName(), entities.size());
        }
    }

    private static void sendWildcardContextualMessage(CommanderCommandSource source, List<? extends Entity> entities) {
        if (entities.size() == 1) {
            source.sendTranslatableMessage("commands.commander.achievement.grant.all.success_single_entity",  ((EntityLiving)entities.get(0)).getDisplayName());
        } else {
            source.sendTranslatableMessage("commands.commander.achievement.grant.all.success_multiple_entities", entities.size());
        }
    }
}
