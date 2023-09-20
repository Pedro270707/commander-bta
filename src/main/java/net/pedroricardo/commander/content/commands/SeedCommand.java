package net.pedroricardo.commander.content.commands;

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
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.AchievementArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class SeedCommand {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HAS_ACHIEVEMENT = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.achievement.grant.exception_already_has_achievement"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("seed")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.seed.success", ((CommanderCommandSource)c.getSource()).getWorld().getRandomSeed()));
                    return CommanderCommandManager.SINGLE_SUCCESS;
                })
        );
    }
}
