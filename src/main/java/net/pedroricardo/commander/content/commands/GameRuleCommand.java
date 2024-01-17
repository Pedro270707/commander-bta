package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.data.gamerule.GameRule;
import net.minecraft.core.data.gamerule.GameRuleBoolean;
import net.minecraft.core.data.registry.Registries;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.GenericGameRuleArgumentType;

@SuppressWarnings("unchecked")
public class GameRuleCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        final LiteralArgumentBuilder<CommanderCommandSource> argumentBuilder = (LiteralArgumentBuilder) LiteralArgumentBuilder.literal("gamerule").requires(source -> ((CommanderCommandSource)source).hasAdmin());
        for (final GameRule<?> gameRule : Registries.GAME_RULES) {
            RequiredArgumentBuilder<CommanderCommandSource, ?> gameRuleValueArgument;
            if (gameRule instanceof GameRuleBoolean) {
                gameRuleValueArgument = RequiredArgumentBuilder.<CommanderCommandSource, Boolean>argument("value", BoolArgumentType.bool())
                        .executes(c -> {
                            c.getSource().getWorld().getLevelData().getGameRules().setValue((GameRule<? super Object>) gameRule, BoolArgumentType.getBool(c, "value"));
                            c.getSource().sendTranslatableMessage("commands.commander.gamerule.set", gameRule.getKey(), BoolArgumentType.getBool(c, "value"));
                            return Command.SINGLE_SUCCESS;
                        });
            } else {
                gameRuleValueArgument = RequiredArgumentBuilder.<CommanderCommandSource, Object>argument("value", GenericGameRuleArgumentType.gameRule(gameRule))
                        .executes(c -> {
                            Object o = c.getArgument("value", Object.class);
                            c.getSource().getWorld().getLevelData().getGameRules().setValue((GameRule<? super Object>) gameRule, o);
                            c.getSource().sendTranslatableMessage("commands.commander.gamerule.set", gameRule.getKey(), o);
                            return Command.SINGLE_SUCCESS;
                        });
            }
            argumentBuilder.then(LiteralArgumentBuilder.<CommanderCommandSource>literal(gameRule.getKey())
                    .executes(c -> {
                        c.getSource().sendTranslatableMessage("commands.commander.gamerule.get", gameRule.getKey(), c.getSource().getWorld().getGameRule(gameRule));
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(gameRuleValueArgument));
        }
        CommandNode commandNode = dispatcher.register(argumentBuilder);
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("gr")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .redirect(commandNode));
    }
}
