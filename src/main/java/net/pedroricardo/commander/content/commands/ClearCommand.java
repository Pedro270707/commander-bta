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
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandManager;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.AchievementArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: add `/clear <entity> [item]` once item stack argument type is added

@SuppressWarnings("unchecked")
public class ClearCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder) LiteralArgumentBuilder.literal("clear")
                .requires(source -> ((CommanderCommandSource)source).hasAdmin())
                .executes(c -> {
                    if (((CommanderCommandSource)c.getSource()).getSender() == null) throw CommanderExceptions.notInWorld().create();

                    EntityPlayer sender = ((CommanderCommandSource)c.getSource()).getSender();

                    int itemsCleared = clearItemsFromEntity(sender);

                    ((CommanderCommandSource) c.getSource()).sendMessage(getMessage(itemsCleared, Collections.singletonList(sender)));

                    return CommanderCommandManager.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.argument("players", EntityArgumentType.players())
                        .executes(c -> {
                            List<? extends Entity> players = c.getArgument("players", EntitySelector.class).get(((CommanderCommandSource)c.getSource()));

                            int itemsCleared = 0;

                            for (Entity player : players) {
                                itemsCleared += clearItemsFromEntity((EntityPlayer)player);
                            }

                            ((CommanderCommandSource) c.getSource()).sendMessage(getMessage(itemsCleared, players));

                            return CommanderCommandManager.SINGLE_SUCCESS;
                        })));
    }

    private static int clearItemsFromEntity(EntityPlayer entityPlayer) {
        int itemsCleared = 0;

        for (int i = 0; i < entityPlayer.inventory.getSizeInventory(); ++i) {
            if (entityPlayer.inventory.getStackInSlot(i) != null) {
                itemsCleared++;
                entityPlayer.inventory.setInventorySlotContents(i, null);
            }
        }

        return itemsCleared;
    }

    private static String getMessage(int itemsCleared, List<? extends Entity> players) {
        if (itemsCleared == 0) return I18n.getInstance().translateKey("commands.commander.clear.failure");

        StringBuilder keyBuilder = new StringBuilder("commands.commander.clear.success_");
        if (itemsCleared > 1) {
            keyBuilder.append("multiple_items_");
        } else if (itemsCleared == 1) {
            keyBuilder.append("single_item_");
        }

        if (players.size() > 1) {
            keyBuilder.append("multiple_entities");
        } else if (players.size() == 1) {
            keyBuilder.append("single_entity");
        }

        return I18n.getInstance().translateKeyAndFormat(keyBuilder.toString(), itemsCleared, players.size());
    }
}
