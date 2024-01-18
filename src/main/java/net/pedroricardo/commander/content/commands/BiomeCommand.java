package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.biome.Biome;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.BiomeArgumentType;
import net.pedroricardo.commander.content.arguments.IntegerCoordinatesArgumentType;
import net.pedroricardo.commander.content.helpers.Coordinates2D;
import net.pedroricardo.commander.content.helpers.IntegerCoordinates;

public class BiomeCommand {
    private static final DynamicCommandExceptionType FAILURE = new DynamicCommandExceptionType(arg -> () -> I18n.getInstance().translateKeyAndFormat("commands.commander.biome.locate.exception_failure", arg));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("biome")
                .requires(CommanderCommandSource::hasAdmin)
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("get")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, IntegerCoordinates>argument("position", IntegerCoordinatesArgumentType.intCoordinates())
                                .executes(c -> {
                                    IntegerCoordinates position = c.getArgument("position", IntegerCoordinates.class);
                                    Biome biome = c.getSource().getWorld().getBlockBiome(position.getX(c.getSource()), position.getY(c.getSource(), true), position.getZ(c.getSource()));
                                    c.getSource().sendTranslatableMessage("commands.commander.biome.get.success", position.getX(c.getSource()), position.getY(c.getSource(), true), position.getZ(c.getSource()), Registries.BIOMES.getKey(biome));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(LiteralArgumentBuilder.<CommanderCommandSource>literal("locate")
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Biome>argument("biome", BiomeArgumentType.biome())
                                .executes(c -> {
                                    Biome biome = c.getArgument("biome", Biome.class);
                                    Coordinates2D biomeLocation = getBiomeCoords(biome, c.getSource());
                                    if (biomeLocation == null) throw FAILURE.create(Registries.BIOMES.getKey(biome));
                                    Vec3d sourcePos = c.getSource().getBlockCoordinates();
                                    if (sourcePos == null) {
                                        c.getSource().sendTranslatableMessage("commands.commander.biome.locate.success", Registries.BIOMES.getKey(biome), biomeLocation.getX(c.getSource()), biomeLocation.getZ(c.getSource()));
                                    } else {
                                        int distance = (int)sourcePos.distanceTo(Vec3d.createVector(biomeLocation.getX(c.getSource()), sourcePos.yCoord, biomeLocation.getZ(c.getSource())));
                                        c.getSource().sendTranslatableMessage(distance == 1 ? "commands.commander.biome.locate.success_in_world_single" : "commands.commander.biome.locate.success_in_world_multiple", Registries.BIOMES.getKey(biome), biomeLocation.getX(c.getSource()), biomeLocation.getZ(c.getSource()), distance);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static Coordinates2D getBiomeCoords(Biome biome, CommanderCommandSource source) {
        int[] xPattern = new int[]{0, 1, 0, -1};
        int[] zPattern = new int[]{1, 0, -1, 0};
        Vec3d sourcePos = source.getBlockCoordinates();
        if (sourcePos == null) sourcePos = Vec3d.createVector(0.0, 0.0, 0.0);
        int runLength = 2;
        int chunkX = (int)(sourcePos.xCoord / 16.0);
        int y = (int)sourcePos.yCoord;
        int chunkZ = (int)(sourcePos.zCoord / 16.0);
        int passes = 1024;
        if (source.getWorld().getBlockBiome((int)sourcePos.xCoord, y, (int)sourcePos.zCoord) == biome) {
            return new Coordinates2D((int)sourcePos.xCoord, (int)sourcePos.zCoord);
        } else {
            --chunkX;
            --chunkZ;

            for(int pass = 0; pass < passes; ++pass) {
                for(int i = 0; i < 4; ++i) {
                    for(int j = runLength - 1; j >= 0; --j) {
                        chunkX += xPattern[i];
                        chunkZ += zPattern[i];
                        if (source.getWorld().getBlockBiome(chunkX * 16, y, chunkZ * 16) == biome) {
                            return new Coordinates2D(chunkX * 16, chunkZ * 16);
                        }
                    }
                }

                ++runLength;
            }

            return null;
        }
    }
}
