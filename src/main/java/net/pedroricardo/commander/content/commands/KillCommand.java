package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class KillCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.kill.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("kill")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                    EntityPlayer sender = source.getSender();

                    if (sender == null) throw CommanderExceptions.notInWorld().create();

                    sender.killPlayer();

                    source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.kill.single_entity", sender.getDisplayName()));

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.argument("entities", EntityArgumentType.entities())
                        .executes(c -> {
                            CommanderCommandSource source = (CommanderCommandSource) c.getSource();
                            EntitySelector entitySelector = c.getArgument("entities", EntitySelector.class);
                            CopyOnWriteArrayList<? extends Entity> entities = new CopyOnWriteArrayList<>(entitySelector.get(source));

                            int entityCount = entities.size();

                            if (entityCount == 0) throw FAILURE.create();
                            else if (entityCount == 1) source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.kill.single_entity", CommanderHelper.getEntityName(entities.get(0))));
                            else source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.kill.multiple_entities", entityCount));

                            for (Entity entity : entities) {
                                if (entity instanceof EntityPlayer) {
                                    ((EntityPlayer) entity).killPlayer();
                                } else if (entity instanceof EntityLiving) {
                                    entity.hurt(null, 100, null);
                                } else {
                                    entity.remove();
                                }
                            }

                            return Command.SINGLE_SUCCESS;
                        })));
    }
}
