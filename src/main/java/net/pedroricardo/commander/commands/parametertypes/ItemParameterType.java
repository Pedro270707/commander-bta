package net.pedroricardo.commander.commands.parametertypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemParameterType extends CommandParameterType {
    public ItemParameterType() {
        super(1);
    }

    @Override
    public List<String> getSuggestions(Minecraft mc, int parameterIndex, int localIndex, String parameter) {
        List<String> suggestions = new ArrayList<>();
        for (Item item : Item.itemsList) {
            if (item == null) continue;
            suggestions.add(item.getKey());
        }
        return suggestions;
    }
}
