package net.pedroricardo.commander.commands;

import net.minecraft.client.Minecraft;

import java.util.List;

public abstract class CommanderCommandParameter {
    private final int expectedParameters;

    public CommanderCommandParameter(int expectedParameters) {
        this.expectedParameters = expectedParameters;
    }

    public int getExpectedParameters() {
        return this.expectedParameters;
    }

    public abstract List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter);
}
