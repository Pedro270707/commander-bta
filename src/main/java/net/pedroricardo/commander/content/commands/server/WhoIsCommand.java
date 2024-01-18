package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.player.EntityPlayer;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

public class WhoIsCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("whois")
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.player())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                            EntityPlayer player = (EntityPlayer) entitySelector.get(source).get(0);

                            source.sendTranslatableMessage("commands.commander.whois.success", CommanderHelper.getEntityName(player), player.username);
                            return Command.SINGLE_SUCCESS;
                        })));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("realname")
                .redirect(command));
    }
}
