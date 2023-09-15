package net.pedroricardo.commander.commands.parametertypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockParameterType extends CommandParameterType {
    public BlockParameterType() {
        super(1);
    }

    @Override
    public List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter) {
        List<String> suggestions = new ArrayList<>();
        for (Block block : Block.blocksList) {
            if (block == null) continue;
            suggestions.add(block.getKey());
        }
        return suggestions;
    }
}
