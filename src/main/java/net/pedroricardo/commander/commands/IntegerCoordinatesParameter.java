package net.pedroricardo.commander.commands;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class IntegerCoordinatesParameter extends CommanderCommandParameter {
    public IntegerCoordinatesParameter() {
        super(3);
    }

    @Override
    public List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter) {
        List<String> list = new ArrayList<>();
        list.add("intCoords");
        return list;
    }
}
