package net.pedroricardo.commander.commands.parameters;

import net.minecraft.core.block.Block;
import net.minecraft.core.item.Item;
import net.minecraft.core.net.command.CommandError;
import net.minecraft.core.net.command.CommandSender;
import net.pedroricardo.commander.commands.CommandParameterTypes;
import net.pedroricardo.commander.commands.parametertypes.CommandParameterType;

public class BlockParameter extends CommandParameter {
    private final Block block;
    public BlockParameter(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return this.block;
    }

    public BlockParameter of(CommandSender commandSender, String parameter) throws CommandError {
        for (Block block : Block.blocksList) {
            if (block.getKey().equals(this.block.getKey())) {
                return new BlockParameter(block);
            }
        }
        throw new CommandError("Invalid item");
    }

    @Override
    public CommandParameterType getType() {
        return CommandParameterTypes.BLOCK;
    }
}
