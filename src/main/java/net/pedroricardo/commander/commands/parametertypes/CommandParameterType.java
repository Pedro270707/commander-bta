package net.pedroricardo.commander.commands.parametertypes;

import net.minecraft.client.Minecraft;
import net.pedroricardo.commander.commands.CommandParameterTypeLike;

import java.util.List;

public abstract class CommandParameterType extends CommandParameterTypeLike {
    private final int expectedParameters;

    public CommandParameterType(int expectedParameters) {
        this.expectedParameters = expectedParameters;
    }

    public int getExpectedParameters() {
        return this.expectedParameters;
    }

    public abstract List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter);
}
