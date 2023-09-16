package net.pedroricardo.commander.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.achievement.Achievement;
import net.pedroricardo.commander.commands.parameters.AchievementArgumentType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class CommanderCommandManager {
    public static int SINGLE_SUCCESS = 1;
    private static final CommandDispatcher<CommanderCommandSource> DISPATCHER = new CommandDispatcher<>();

    static {
        DISPATCHER.register((LiteralArgumentBuilder)(((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("achievement"))
                .then(((LiteralArgumentBuilder)LiteralArgumentBuilder.<CommanderCommandSource>literal("grant")
                        ).then(RequiredArgumentBuilder.argument("achievement", AchievementArgumentType.achievementParameter())
                            .executes(c -> {
                                List<Achievement> achievements = new ArrayList<>();
                                achievements.add(c.getArgument("achievement", Achievement.class));
                                while (achievements.get(achievements.size() - 1).parent != null) {
                                    achievements.add(achievements.get(achievements.size() - 1).parent);
                                }
                                for (int i = 0; i < achievements.size(); i++) {
                                    ((CommanderCommandSource)c.getSource()).getSender().triggerAchievement(achievements.get(achievements.size() - 1 - i));
                                }
                                return SINGLE_SUCCESS;
                            })))));
    }

    public static void execute(String s, CommanderCommandSource commandSource) throws CommandSyntaxException {
        DISPATCHER.execute(s, commandSource);
    }

    public static CommandDispatcher<CommanderCommandSource> getDispatcher() {
        return DISPATCHER;
    }

    public static void init() {
    }
}
