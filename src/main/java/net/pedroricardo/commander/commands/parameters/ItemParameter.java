package net.pedroricardo.commander.commands.parameters;

import net.minecraft.core.item.Item;
import net.minecraft.core.net.command.CommandError;
import net.minecraft.core.net.command.CommandSender;
import net.pedroricardo.commander.commands.CommandParameterTypes;
import net.pedroricardo.commander.commands.parametertypes.CommandParameterType;

public class ItemParameter extends CommandParameter {
    private final Item item;
    public ItemParameter(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return this.item;
    }

    public ItemParameter of(CommandSender commandSender, String parameter) throws CommandError {
        for (Item item : Item.itemsList) {
            if (item.getKey().equals(this.item.getKey())) {
                return new ItemParameter(item);
            }
        }
        throw new CommandError("Invalid item");
    }

    @Override
    public CommandParameterType getType() {
        return CommandParameterTypes.ITEM;
    }
}
