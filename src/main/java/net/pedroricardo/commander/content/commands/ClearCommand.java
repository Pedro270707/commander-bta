package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.nbt.Tag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.EntityPlayer;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.lang.I18n;
import net.pedroricardo.commander.Commander;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.ItemStackArgumentType;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;
import net.pedroricardo.commander.content.helpers.EntitySelector;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClearCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.clear.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("clear")
                .requires(CommanderCommandSource::hasAdmin)
                .executes(c -> {
                    CommanderCommandSource source = c.getSource();
                    EntityPlayer sender = source.getSender();

                    if (sender == null) throw CommanderExceptions.notInWorld().create();

                    int itemsCleared = clearItemsFromEntity(sender, null);

                    if (itemsCleared == 0) throw FAILURE.create();
                    source.sendMessage(getMessage(itemsCleared, Collections.singletonList(sender)));

                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("players", EntityArgumentType.players())
                        .executes(c -> {
                            CommanderCommandSource source = c.getSource();
                            List<? extends Entity> players = c.getArgument("players", EntitySelector.class).get(source);

                            int itemsCleared = 0;

                            for (Entity player : players) {
                                itemsCleared += clearItemsFromEntity((EntityPlayer)player, null);
                            }

                            if (itemsCleared == 0) throw FAILURE.create();
                            source.sendMessage(getMessage(itemsCleared, players));

                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, ItemStack>argument("item", ItemStackArgumentType.itemStack())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    List<? extends Entity> players = c.getArgument("players", EntitySelector.class).get(source);
                                    ItemStack itemStack = c.getArgument("item", ItemStack.class);

                                    int itemsCleared = 0;

                                    for (Entity player : players) {
                                        itemsCleared += clearItemsFromEntity((EntityPlayer)player, itemStack);
                                    }

                                    if (itemsCleared == 0) throw FAILURE.create();
                                    source.sendMessage(getMessage(itemsCleared, players));

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static int clearItemsFromEntity(EntityPlayer entityPlayer, @Nullable ItemStack itemStack) {
        int itemsCleared = 0;

        for (int i = 0; i < entityPlayer.inventory.getSizeInventory(); ++i) {
            ItemStack stackInSlot = entityPlayer.inventory.getStackInSlot(i);
            if (stackInSlot != null && (itemStack == null || matchesItemStack(itemStack, stackInSlot))) {
                itemsCleared++;
                entityPlayer.inventory.setInventorySlotContents(i, null);
            }
        }

        return itemsCleared;
    }

    private static boolean matchesItemStack(ItemStack checkedStack, ItemStack input) {
        ItemStack checkedStackCopy = checkedStack.copy();
        ItemStack inputCopy = input.copy();
        checkedStackCopy.stackSize = 1;
        inputCopy.stackSize = 1;
        if (checkedStackCopy.getData().getValues().isEmpty()) {
            return checkedStackCopy.isStackEqual(inputCopy);
        }
        Commander.LOGGER.info(checkedStackCopy.itemID + ", " + inputCopy.itemID + ", " + (checkedStackCopy.itemID == inputCopy.itemID));
        Commander.LOGGER.info(checkedStackCopy.getMetadata() + ", " + inputCopy.getMetadata() + ", " + (checkedStackCopy.getMetadata() == inputCopy.getMetadata()));
        Commander.LOGGER.info(checkedStackCopy.getData().getValues() + ", " + inputCopy.getData().getValues() + ", " + inputCopy.getData().getValues().containsAll(checkedStackCopy.getData().getValues()));
        return checkedStackCopy.isStackEqual(inputCopy) && matchesTag(checkedStackCopy, inputCopy);
    }

    private static boolean matchesTag(ItemStack checkedStack, ItemStack input) {
        for (Map.Entry<String, Tag<?>> entry : checkedStack.getData().getValue().entrySet()) {
            if (!input.getData().getValue().containsKey(entry.getKey())
            || (input.getData().getValue().get(entry.getKey()).getValue() != entry.getValue().getValue() && !input.getData().getValue().get(entry.getKey()).getValue().equals(entry.getValue().getValue()))) {
                return false;
            }
        }
        return true;
    }

    private static String getMessage(int itemsCleared, List<? extends Entity> players) {
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
