package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundCategory;
import net.minecraft.core.util.phys.Vec3d;
import net.minecraft.core.world.Dimension;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.DimensionArgumentType;
import net.pedroricardo.commander.content.arguments.SoundCategoryArgumentType;
import net.pedroricardo.commander.content.arguments.SoundTypeArgumentType;
import net.pedroricardo.commander.content.arguments.PositionArgumentType;
import net.pedroricardo.commander.content.helpers.DoublePos;
import org.jetbrains.annotations.Nullable;

public class PlaySoundCommand {
    private static final DynamicCommandExceptionType FAILURE = new DynamicCommandExceptionType((arg) -> () -> I18n.getInstance().translateKeyAndFormat("commands.commander.playsound.exception_failure", arg));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("playsound")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("sound", SoundTypeArgumentType.soundType())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, SoundCategory>argument("category", SoundCategoryArgumentType.soundCategory())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, DoublePos>argument("position", PositionArgumentType.pos())
                                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Float>argument("volume", FloatArgumentType.floatArg(0.0f))
                                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Float>argument("pitch", FloatArgumentType.floatArg(0.0f, 2.0f))
                                                        .executes(c -> {
                                                            String sound = c.getArgument("sound", String.class);
                                                            SoundCategory category = c.getArgument("category", SoundCategory.class);
                                                            DoublePos position = c.getArgument("position", DoublePos.class);
                                                            if (execute(c, c.getArgument("sound", String.class), c.getArgument("category", SoundCategory.class), c.getArgument("position", DoublePos.class), c.getArgument("volume", Float.class), c.getArgument("pitch", Float.class), null)) {
                                                                c.getSource().sendTranslatableMessage("commands.commander.playsound.success", sound, category, position.getX(c.getSource()), position.getY(c.getSource(), true), position.getZ(c.getSource()));
                                                                return Command.SINGLE_SUCCESS;
                                                            } else {
                                                                throw FAILURE.create(c.getArgument("sound", String.class));
                                                            }
                                                        })
                                                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Dimension>argument("dimension", DimensionArgumentType.dimension())
                                                                .executes(c -> {
                                                                    String sound = c.getArgument("sound", String.class);
                                                                    SoundCategory category = c.getArgument("category", SoundCategory.class);
                                                                    DoublePos position = c.getArgument("position", DoublePos.class);
                                                                    if (execute(c, sound, category, position, c.getArgument("volume", Float.class), c.getArgument("pitch", Float.class), c.getArgument("dimension", Dimension.class))) {
                                                                        c.getSource().sendTranslatableMessage("commands.commander.playsound.success", sound, category, position.getX(c.getSource()), position.getY(c.getSource(), true), position.getZ(c.getSource()));
                                                                        return Command.SINGLE_SUCCESS;
                                                                    } else {
                                                                        throw FAILURE.create(c.getArgument("sound", String.class));
                                                                    }
                                                                }))))))));
    }

    private static boolean execute(CommandContext<CommanderCommandSource> c, String sound, SoundCategory category, DoublePos position, float volume, float pitch, @Nullable Dimension dimension) throws CommandSyntaxException {
        float x = (float) position.getX(c.getSource());
        float y = (float) position.getY(c.getSource(), true);
        float z = (float) position.getZ(c.getSource());
        if (dimension != null) {
            return c.getSource().playSound(sound, category, x, y, z, volume, pitch, dimension.id);
        } else {
            return c.getSource().playSound(sound, category, x, y, z, volume, pitch);
        }
    }
}
