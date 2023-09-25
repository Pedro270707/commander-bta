package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class KillCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("kill")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    EntityPlayer sender = ((CommanderCommandSource)c.getSource()).getSender();

                    if (sender == null) throw CommanderExceptions.notInWorld().create();

                    sender.killPlayer();

                    ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.kill.single_entity", sender.getDisplayName()));

                    return CommanderCommandManager.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.argument("entities", EntityArgumentType.entities())
                        .executes(c -> {
                            EntitySelector entitySelector = c.getArgument("entities", EntitySelector.class);
                            CopyOnWriteArrayList<? extends Entity> entities = new CopyOnWriteArrayList<>(entitySelector.get((CommanderCommandSource)c.getSource()));

                            int entityCount = entities.size();

                            if (entityCount == 0) ((CommanderCommandSource)c.getSource()).sendMessage("§e" + I18n.getInstance().translateKey("commands.commander.kill.failure"));
                            else if (entityCount == 1) ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.kill.single_entity", CommanderHelper.getEntityName(entities.get(0))));
                            else ((CommanderCommandSource)c.getSource()).sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.kill.multiple_entities", entityCount));

                            for (Entity entity : entities) {
                                if (entity instanceof EntityPlayer) {
                                    ((EntityPlayer) entity).killPlayer();
                                } else if (entity instanceof EntityLiving) {
                                    entity.hurt(null, 100, null);
                                } else {
                                    entity.remove();
                                }
                            }

                            return CommanderCommandManager.SINGLE_SUCCESS;
                        })));
    }
}
