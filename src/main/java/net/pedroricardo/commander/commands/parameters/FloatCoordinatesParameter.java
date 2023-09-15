package net.pedroricardo.commander.commands.parameters;

import net.minecraft.core.net.command.CommandError;
import net.minecraft.core.net.command.CommandSender;
import net.pedroricardo.commander.commands.CommandParameterTypes;
import net.pedroricardo.commander.commands.parametertypes.CommandParameterType;

public class FloatCoordinatesParameter extends CommandParameter {
    private final float x;
    private final float y;
    private final float z;
    public FloatCoordinatesParameter(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public FloatCoordinatesParameter of(CommandSender commandSender, String parameter) throws CommandError {
        String[] splitString = parameter.split(" ");
        if (splitString.length == 3) {
            try {
                return new FloatCoordinatesParameter(Float.parseFloat(splitString[0]), Float.parseFloat(splitString[1]), Float.parseFloat(splitString[2]));
            } catch (NumberFormatException e) {
                throw new CommandError("Invalid coordinates");
            }
        }
        throw new CommandError("Invalid coordinates");
    }

    @Override
    public CommandParameterType getType() {
        return CommandParameterTypes.FLOAT_COORDINATES;
    }
}
