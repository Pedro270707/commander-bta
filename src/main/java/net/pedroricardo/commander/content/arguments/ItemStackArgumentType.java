package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.block.Block;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.helpers.BlockArgumentParser;
import net.pedroricardo.commander.content.helpers.ItemStackArgumentParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ItemStackArgumentType implements ArgumentType<ItemStack> {
    private static final List<String> EXAMPLES = Arrays.asList("tool.sword.iron", "tool.sword.iron{name:\"Sword\", overrideName: true}", "tile.log");

    public static ArgumentType<ItemStack> itemStack() {
        return new ItemStackArgumentType();
    }

    @Override
    public ItemStack parse(StringReader reader) throws CommandSyntaxException {
        ItemStackArgumentParser parser = new ItemStackArgumentParser(reader);
        return parser.parse();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        ItemStackArgumentParser parser = new ItemStackArgumentParser(stringReader);
        try {
            parser.parse();
        } catch (CommandSyntaxException ignored) {}
        return parser.fillSuggestions(builder, suggestionsBuilder -> {
            String remaining = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
            for (Item item : Item.itemsList) {
                if (item == null) continue;
                CommanderHelper.getStringToSuggest(item.getKey(), remaining).ifPresent(suggestionsBuilder::suggest);
            }
            suggestionsBuilder.buildFuture();
        });
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
