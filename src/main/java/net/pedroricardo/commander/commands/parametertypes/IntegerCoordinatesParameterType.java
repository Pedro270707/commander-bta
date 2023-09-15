package net.pedroricardo.commander.commands.parametertypes;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class IntegerCoordinatesParameterType extends CommandParameterType {
    public IntegerCoordinatesParameterType() {
        super(3);
    }

    @Override
    public List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter) {
        List<String> parameters = new ArrayList<>();
        switch (localIndex) {
            case 2:
                if (mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entity != null)
                        parameters.add(String.valueOf(Math.round(mc.objectMouseOver.entity.x)));
                    else
                        parameters.add(String.valueOf(Math.round(mc.objectMouseOver.x)));
                } else {
                    parameters.add(String.valueOf(Math.round(mc.thePlayer.z)));
                }
                break;
            case 1:
                if (mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entity != null) {
                        parameters.add(String.valueOf(Math.round(mc.objectMouseOver.entity.y)));
                        parameters.add(Math.round(mc.objectMouseOver.entity.y) + " " + Math.round(mc.objectMouseOver.entity.z));
                    } else {
                        parameters.add(String.valueOf(Math.round(mc.objectMouseOver.y)));
                        parameters.add(Math.round(mc.objectMouseOver.y) + " " + Math.round(mc.objectMouseOver.z));
                    }
                } else {
                    parameters.add(String.valueOf(Math.round(mc.thePlayer.y)));
                    parameters.add(Math.round(mc.thePlayer.y) + " " + Math.round(mc.thePlayer.z));
                }
                break;
            default:
                if (mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entity != null) {
                        parameters.add(String.valueOf(Math.round(mc.objectMouseOver.entity.x)));
                        parameters.add(Math.round(mc.objectMouseOver.entity.x) + " " + Math.round(mc.objectMouseOver.entity.y));
                        parameters.add(Math.round(mc.objectMouseOver.entity.x) + " " + Math.round(mc.objectMouseOver.entity.y) + " " + Math.round(mc.objectMouseOver.entity.z));
                    } else {
                        parameters.add(String.valueOf(Math.round(mc.objectMouseOver.x)));
                        parameters.add(Math.round(mc.objectMouseOver.x) + " " + Math.round(mc.objectMouseOver.y));
                        parameters.add(Math.round(mc.objectMouseOver.x) + " " + Math.round(mc.objectMouseOver.y) + " " + Math.round(mc.objectMouseOver.z));
                    }
                } else {
                    parameters.add(String.valueOf(Math.round(mc.thePlayer.x)));
                    parameters.add(Math.round(mc.thePlayer.x) + " " + Math.round(mc.thePlayer.y));
                    parameters.add(Math.round(mc.thePlayer.x) + " " + Math.round(mc.thePlayer.y) + " " + Math.round(mc.thePlayer.z));
                }
        }
        return parameters;
    }
}
