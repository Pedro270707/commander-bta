package net.pedroricardo.commander.commands;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class FloatCoordinatesParameter extends CommanderCommandParameter {
    public FloatCoordinatesParameter() {
        super(3);
    }

    /*
    public static FloatCoordinatesParameter of(String str) {
        String[] splitString = str.split(" ");
        if (splitString.length == 3) {
            try {
                return new FloatCoordinatesParameter(Float.parseFloat(splitString[0]), Float.parseFloat(splitString[1]), Float.parseFloat(splitString[2]));
            } catch (NumberFormatException e) {
                throw new CommandError("Invalid coordinates");
            }
        }
        throw new CommandError("Invalid coordinates");
    }
    */

    @Override
    public List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter) {
        List<String> parameters = new ArrayList<>();
        switch (localIndex) {
            case 2:
                if (mc.objectMouseOver != null) {
                    parameters.add(String.valueOf(mc.objectMouseOver.z));
                } else {
                    parameters.add(String.valueOf(mc.thePlayer.z));
                }
                break;
            case 1:
                if (mc.objectMouseOver != null) {
                    parameters.add(String.valueOf(mc.objectMouseOver.y));
                    parameters.add(mc.objectMouseOver.y + " " + mc.objectMouseOver.z);
                } else {
                    parameters.add(String.valueOf(mc.thePlayer.y));
                    parameters.add(mc.thePlayer.y + " " + mc.thePlayer.z);
                }
                break;
            default:
                if (mc.objectMouseOver != null) {
                    parameters.add(String.valueOf(mc.objectMouseOver.x));
                    parameters.add(mc.objectMouseOver.x + " " + mc.objectMouseOver.y);
                    parameters.add(mc.objectMouseOver.x + " " + mc.objectMouseOver.y + " " + mc.objectMouseOver.z);
                } else {
                    parameters.add(String.valueOf(mc.thePlayer.x));
                    parameters.add(mc.thePlayer.x + " " + mc.thePlayer.y);
                    parameters.add(mc.thePlayer.x + " " + mc.thePlayer.y + " " + mc.thePlayer.z);
                }
        }
        return parameters;
    }
}
