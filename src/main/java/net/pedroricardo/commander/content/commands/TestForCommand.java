package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.nbt.CompoundTag;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.pedroricardo.commander.CommanderHelper;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BlockArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.*;

import java.util.List;

public class TestForCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("testfor")
                .requires(CommanderCommandSource::hasAdmin)
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("entity")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("entities", EntityArgumentType.entities())
                                .executes(c -> {
                                    List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get(c.getSource());
                                    c.getSource().sendTranslatableMessage("commands.commander.testfor.entity." + (entities.isEmpty() ? "none" : entities.size() == 1 ? "single" : "multiple"), entities.size());
                                    return entities.size();
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("block")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, BlockInput>argument("block", BlockArgumentType.block())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("second", IntegerCoordinatesArgumentType.intCoordinates())
                                        .executes(c -> {
                                            BlockInput block = c.getArgument("block", BlockInput.class);
                                            IntegerCoordinates secondPos = c.getArgument("second", IntegerCoordinates.class);
                                            CompoundTag blockInWorldCompoundTag = new CompoundTag();
                                            TileEntity tileEntity = c.getSource().getWorld().getBlockTileEntity(secondPos.getX(c.getSource()), secondPos.getY(c.getSource(), true), secondPos.getZ(c.getSource()));
                                            if (tileEntity != null) tileEntity.writeToNBT(blockInWorldCompoundTag);
                                            if (c.getSource().getWorld().getBlock(secondPos.getX(c.getSource()), secondPos.getY(c.getSource(), true), secondPos.getZ(c.getSource())) == block.getBlock()
                                            && c.getSource().getWorld().getBlockMetadata(secondPos.getX(c.getSource()), secondPos.getY(c.getSource(), true), secondPos.getZ(c.getSource())) == block.getMetadata()
                                            && (blockInWorldCompoundTag == block.getTag() || block.getTag().getValues().isEmpty())) {
                                                c.getSource().sendTranslatableMessage("commands.commander.testfor.block.success");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            c.getSource().sendTranslatableMessage("commands.commander.testfor.block.failure");
                                            return 0;
                                        })))
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("first", IntegerCoordinatesArgumentType.intCoordinates())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("second", IntegerCoordinatesArgumentType.intCoordinates())
                                        .executes(c -> {
                                            IntegerCoordinates firstPos = c.getArgument("first", IntegerCoordinates.class);
                                            IntegerCoordinates secondPos = c.getArgument("second", IntegerCoordinates.class);

                                            Block firstBlock = c.getSource().getWorld().getBlock(firstPos.getX(c.getSource()), firstPos.getY(c.getSource(), true), firstPos.getZ(c.getSource()));
                                            Block secondBlock = c.getSource().getWorld().getBlock(secondPos.getX(c.getSource()), secondPos.getY(c.getSource(), true), secondPos.getZ(c.getSource()));

                                            if (firstBlock != secondBlock) {
                                                c.getSource().sendTranslatableMessage("commands.commander.testfor.block.failure");
                                                return 0;
                                            }

                                            int firstMetadata = c.getSource().getWorld().getBlockMetadata(firstPos.getX(c.getSource()), firstPos.getY(c.getSource(), true), firstPos.getZ(c.getSource()));
                                            int secondMetadata = c.getSource().getWorld().getBlockMetadata(secondPos.getX(c.getSource()), secondPos.getY(c.getSource(), true), secondPos.getZ(c.getSource()));

                                            if (firstMetadata != secondMetadata) {
                                                c.getSource().sendTranslatableMessage("commands.commander.testfor.block.failure");
                                                return 0;
                                            }

                                            CompoundTag firstTag = new CompoundTag();
                                            TileEntity firstTileEntity = c.getSource().getWorld().getBlockTileEntity(firstPos.getX(c.getSource()), firstPos.getY(c.getSource(), true), firstPos.getZ(c.getSource()));
                                            if (firstTileEntity != null) firstTileEntity.writeToNBT(firstTag);
                                            CompoundTag secondTag = new CompoundTag();
                                            TileEntity secondTileEntity = c.getSource().getWorld().getBlockTileEntity(secondPos.getX(c.getSource()), secondPos.getY(c.getSource(), true), secondPos.getZ(c.getSource()));
                                            if (secondTileEntity != null) secondTileEntity.writeToNBT(secondTag);

                                            if (!CommanderHelper.blockEntitiesAreEqual(firstTag, secondTag)) {
                                                c.getSource().sendTranslatableMessage("commands.commander.testfor.block.failure");
                                                return 0;
                                            }

                                            c.getSource().sendTranslatableMessage("commands.commander.testfor.block.success");
                                            return Command.SINGLE_SUCCESS;
                                        })))));
    }
}
