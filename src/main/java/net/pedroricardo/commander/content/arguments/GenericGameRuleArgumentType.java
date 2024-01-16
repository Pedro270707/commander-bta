package net.pedroricardo.commander.content.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.data.gamerule.GameRule;
import net.pedroricardo.commander.content.exceptions.CommanderExceptions;

public class GenericGameRuleArgumentType implements ArgumentType<Object> {
    private final GameRule<?> gameRule;

    private GenericGameRuleArgumentType(GameRule<?> gameRule) {
        this.gameRule = gameRule;
    }

    public static GenericGameRuleArgumentType gameRule(GameRule<?> gameRule) {
        return new GenericGameRuleArgumentType(gameRule);
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        StringBuilder read = new StringBuilder();
        Object value = null;
        while (reader.canRead() && (StringReader.isQuotedStringStart(reader.peek()) || StringReader.isAllowedInUnquotedString(reader.peek())) && value == null) {
            read.append(reader.readString());
            value = this.gameRule.parseFromString(read.toString());
        }
        System.out.println(read);
        if (value != null) return value;
        throw CommanderExceptions.invalidGameRuleValue().create();
    }
}
