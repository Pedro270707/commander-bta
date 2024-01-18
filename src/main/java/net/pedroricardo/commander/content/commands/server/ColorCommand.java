package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.IServerCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class ColorCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        CommandNode<CommanderCommandSource> command = dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("color")
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("get")
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                            EntityPlayerMP player = (EntityPlayerMP) source.getSender();
                            if (player == null) throw CommanderExceptions.notInWorld().create();

                            TextFormatting color = TextFormatting.get(player.chatColor);
                            source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.color.get.success", color + color.getNames()[0]));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.player())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

                                    EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                    EntityPlayerMP player = (EntityPlayerMP) entitySelector.get(source).get(0);

                                    TextFormatting color = TextFormatting.get(player.chatColor);
                                    source.sendMessage(I18n.getInstance().translateKeyAndFormat("commands.commander.color.get.success_other", color + color.getNames()[0]));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("set")
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("white")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 0);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("orange")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 1);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("magenta")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 2);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("light_blue")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 3);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("yellow")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 4);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("lime")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 5);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("pink")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 6);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("gray")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 7);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("light_gray")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 8);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("cyan")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 9);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("purple")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 10);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("blue")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 11);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("brown")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 12);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("green")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 13);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("red")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 14);
                                }))
                        .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("black")
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    return setColor((EntityPlayerMP) source.getSender(), source, (byte) 15);
                                }))
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.players())
                                .requires(CommanderCommandSource::hasAdmin)
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("white")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 0);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("orange")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 1);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("magenta")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 2);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("light_blue")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 3);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("yellow")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 4);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("lime")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 5);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("pink")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 6);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("gray")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 7);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("light_gray")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 8);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("cyan")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 9);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("purple")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 10);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("blue")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 11);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("brown")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 12);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("green")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 13);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("red")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 14);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("black")
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            for (Entity entity : entities) {
                                                setColor((EntityPlayerMP) entity, source, (byte) 15);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })))));
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("colour")
                .redirect(command));
    }

    private static int setColor(EntityPlayerMP player, CommanderCommandSource source, byte colorId) throws CommandSyntaxException {
        if (!(source instanceof IServerCommandSource)) throw CommanderExceptions.multiplayerWorldOnly().create();

        if (player == null) throw CommanderExceptions.notInWorld().create();

        player.chatColor = colorId;
        TextFormatting color = TextFormatting.get(colorId);
        if (player == source.getSender()) {
            source.sendTranslatableMessage("commands.commander.color.set.success", color + color.getNames()[0]);
        } else {
            source.sendTranslatableMessage("commands.commander.color.set.success_other", CommanderHelper.getEntityName(player), color + color.getNames()[0]);
            source.sendTranslatableMessage(player, "commands.commander.color.set.success_receiver", color + color.getNames()[0]);
        }
        return Command.SINGLE_SUCCESS;
    }
}
