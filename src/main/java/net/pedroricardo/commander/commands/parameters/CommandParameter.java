package net.pedroricardo.commander.commands.parameters;

import net.minecraft.core.net.command.CommandSender;
import net.pedroricardo.commander.commands.parametertypes.CommandParameterType;

public abstract class CommandParameter {
    public abstract CommandParameter of(CommandSender commandSender, String parameter);
    public abstract CommandParameterType getType();
}
