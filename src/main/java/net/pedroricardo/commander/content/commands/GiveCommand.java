package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.item.ItemStack;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.ItemStackArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class GiveCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("give")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("target", EntityArgumentType.players())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, ItemStack>argument("item", ItemStackArgumentType.itemStack())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    ItemStack itemStack = c.getArgument("item", ItemStack.class);
                                    int amount = itemStack.stackSize;
                                    EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                    List<? extends Entity> entities = entitySelector.get(source);

                                    for (Entity player : entities) {
                                        ((EntityPlayer)player).inventory.insertItem(itemStack, true);
                                        if (itemStack.stackSize > 0) {
                                            ((EntityPlayer)player).dropPlayerItem(itemStack);
                                        }
                                    }

                                    if (entities.size() == 1) {
                                        source.sendTranslatableMessage("commands.commander.give.success_single_entity", CommanderHelper.getEntityName(entities.get(0)), amount, itemStack.getDisplayName());
                                    } else {
                                        source.sendTranslatableMessage("commands.commander.give.success_single_entity", entities.size(), amount, itemStack.getDisplayName());
                                    }

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("amount", IntegerArgumentType.integer(1, 6400))
                                        .executes(c -> {
                                            CommanderCommandSource source = c.getSource();
                                            ItemStack itemStack = c.getArgument("item", ItemStack.class);
                                            EntitySelector entitySelector = c.getArgument("target", EntitySelector.class);
                                            List<? extends Entity> entities = entitySelector.get(source);
                                            int amount = c.getArgument("amount", Integer.class);

                                            for (Entity player : entities) {
                                                int incompleteStack = amount % 64;
                                                for (int i = 0; i < (amount - incompleteStack) / 64; i++) {
                                                    ItemStack itemStack1 = ItemStack.copyItemStack(itemStack);
                                                    itemStack1.stackSize = 64;
                                                    ((EntityPlayer) player).inventory.insertItem(itemStack1, true);
                                                    if (itemStack1.stackSize > 0) {
                                                        ((EntityPlayer) player).dropPlayerItem(itemStack1);
                                                    }
                                                }
                                                if (incompleteStack > 0) {
                                                    ItemStack itemStack1 = ItemStack.copyItemStack(itemStack);
                                                    itemStack1.stackSize = incompleteStack;
                                                    ((EntityPlayer) player).inventory.insertItem(itemStack1, true);
                                                    if (itemStack1.stackSize > 0) {
                                                        ((EntityPlayer) player).dropPlayerItem(itemStack1);
                                                    }
                                                }
                                            }

                                            if (entities.size() == 1) {
                                                source.sendTranslatableMessage("commands.commander.give.success_single_entity", CommanderHelper.getEntityName(entities.get(0)), amount, itemStack.getDisplayName());
                                            } else {
                                                source.sendTranslatableMessage("commands.commander.give.success_single_entity", entities.size(), amount, itemStack.getDisplayName());
                                            }

                                            return Command.SINGLE_SUCCESS;
                                        })))));
    }
}
