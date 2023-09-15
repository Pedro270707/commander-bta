package net.pedroricardo.commander.commands.parameters;

import net.minecraft.core.net.command.CommandError;
import net.minecraft.core.net.command.CommandSender;
import net.pedroricardo.commander.commands.CommandParameterTypes;
import net.pedroricardo.commander.commands.parametertypes.CommandParameterType;

public class IntegerCoordinatesParameter extends CommandParameter {
    private final int x;
    private final int y;
    private final int z;
    public IntegerCoordinatesParameter(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public IntegerCoordinatesParameter of(CommandSender commandSender, String parameter) throws CommandError {
        String[] splitString = parameter.split(" ");
        if (splitString.length == 3) {
            try {
                return new IntegerCoordinatesParameter(Integer.parseInt(splitString[0]), Integer.parseInt(splitString[1]), Integer.parseInt(splitString[2]));
            } catch (NumberFormatException e) {
                throw new CommandError("Invalid coordinates");
            }
        }
        throw new CommandError("Invalid coordinates");
    }

    @Override
    public CommandParameterType getType() {
        return CommandParameterTypes.INTEGER_COORDINATES;
    }
}
