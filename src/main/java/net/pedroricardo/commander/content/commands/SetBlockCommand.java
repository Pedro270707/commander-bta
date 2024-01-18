package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.Tag;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.*;
import net.pedroricardo.commander.content.helpers.BlockInput;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

import java.util.Map;

public class SetBlockCommand {
    private static final SimpleCommandExceptionType FAILURE = new SimpleCommandExceptionType(() -> I18n.getInstance().translateKey("commands.commander.setblock.exception_failure"));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("setblock")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, BlockInput>argument("block", BlockArgumentType.block())
                                .executes(c -> {
                                    CommanderCommandSource source = c.getSource();
                                    IntegerCoordinates coordinates = c.getArgument("position", IntegerCoordinates.class);
                                    BlockInput blockInput = c.getArgument("block", BlockInput.class);
                                    World world = source.getWorld();

                                    int x = coordinates.getX(source);
                                    int y = coordinates.getY(source, true);
                                    int z = coordinates.getZ(source);

                                    if (!world.isBlockLoaded(x, y, z)) {
                                        throw FAILURE.create();
                                    } else {
                                        world.setBlockAndMetadataWithNotify(x, y, z, blockInput.getBlockId(), blockInput.getMetadata());
                                        TileEntity tileEntity = source.getWorld().getBlockTileEntity(x, y, z);
                                        if (tileEntity != null) {
                                            CompoundTag tag = new CompoundTag();
                                            tileEntity.writeToNBT(tag);
                                            for (Map.Entry<String, Tag<?>> entry : blockInput.getTag().getValue().entrySet()) {
                                                tag.put(entry.getKey(), entry.getValue());
                                            }
                                            tileEntity.readFromNBT(tag);
                                        }
                                        source.sendTranslatableMessage("commands.commander.setblock.success", coordinates.getX(source), coordinates.getY(source, true), coordinates.getZ(source));
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
