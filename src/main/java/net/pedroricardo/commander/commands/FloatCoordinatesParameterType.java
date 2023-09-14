package net.pedroricardo.commander.commands;

import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FloatCoordinatesParameterType extends CommanderCommandParameterType {
    public FloatCoordinatesParameterType() {
        super(3);
    }

    /*
    public static FloatCoordinatesParameterType of(String str) {
        String[] splitString = str.split(" ");
        if (splitString.length == 3) {
            try {
                return new FloatCoordinatesParameterType(Float.parseFloat(splitString[0]), Float.parseFloat(splitString[1]), Float.parseFloat(splitString[2]));
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
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.######", symbols);
        switch (localIndex) {
            case 2:
                if (mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entity != null)
                        parameters.add(df.format(mc.objectMouseOver.entity.x));
                    else
                        parameters.add(df.format(mc.objectMouseOver.x));
                } else {
                    parameters.add(df.format(mc.thePlayer.z));
                }
                break;
            case 1:
                if (mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entity != null) {
                        parameters.add(df.format(mc.objectMouseOver.entity.y));
                        parameters.add(df.format(mc.objectMouseOver.entity.y) + " " + df.format(mc.objectMouseOver.entity.z));
                    } else {
                        parameters.add(df.format(mc.objectMouseOver.y));
                        parameters.add(df.format(mc.objectMouseOver.y) + " " + df.format(mc.objectMouseOver.z));
                    }
                } else {
                    parameters.add(df.format(mc.thePlayer.y));
                    parameters.add(df.format(mc.thePlayer.y) + " " + df.format(mc.thePlayer.z));
                }
                break;
            default:
                if (mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entity != null) {
                        parameters.add(df.format(mc.objectMouseOver.entity.x));
                        parameters.add(df.format(mc.objectMouseOver.entity.x) + " " + df.format(mc.objectMouseOver.entity.y));
                        parameters.add(df.format(mc.objectMouseOver.entity.x) + " " + df.format(mc.objectMouseOver.entity.y) + " " + df.format(mc.objectMouseOver.entity.z));
                    } else {
                        parameters.add(df.format(mc.objectMouseOver.x));
                        parameters.add(df.format(mc.objectMouseOver.x) + " " + df.format(mc.objectMouseOver.y));
                        parameters.add(df.format(mc.objectMouseOver.x) + " " + df.format(mc.objectMouseOver.y) + " " + df.format(mc.objectMouseOver.z));
                    }
                } else {
                    parameters.add(df.format(mc.thePlayer.x));
                    parameters.add(df.format(mc.thePlayer.x) + " " + df.format(mc.thePlayer.y));
                    parameters.add(df.format(mc.thePlayer.x) + " " + df.format(mc.thePlayer.y) + " " + df.format(mc.thePlayer.z));
                }
        }
        return parameters;
    }
}
