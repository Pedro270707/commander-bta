package net.pedroricardo.commander.content.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.server.net.ChatEmotes;
import net.pedroricardo.commander.content.CommanderCommandSource;

import java.util.ArrayList;
import java.util.Map;

public class EmotesCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("emotes")
                .executes(c -> {
                    CommanderCommandSource source = c.getSource();
                    source.sendTranslatableMessage("commands.commander.emotes.success");
                    ArrayList<Map.Entry<String, Character>> entryList = new ArrayList<Map.Entry<String, Character>>(ChatEmotes.getEmotes().entrySet());
                    entryList.sort(Map.Entry.comparingByKey());
                    for (Map.Entry<String, Character> entry : entryList) {
                        source.sendMessage(":" + TextFormatting.CYAN + entry.getKey().substring(1, entry.getKey().length() - 1) + TextFormatting.RESET + ":" + " -> " + entry.getValue());
                    }
                    return entryList.size();
                }));
    }
}
