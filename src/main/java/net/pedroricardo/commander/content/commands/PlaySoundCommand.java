package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.core.lang.I18n;
import net.minecraft.core.sound.SoundCategory;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.SoundCategoryArgumentType;
import net.pedroricardo.commander.content.arguments.SoundTypeArgumentType;
import net.pedroricardo.commander.content.arguments.Vec3dArgumentType;
import net.pedroricardo.commander.content.helpers.DoubleCoordinates;

public class PlaySoundCommand {
    private static final DynamicCommandExceptionType FAILURE = new DynamicCommandExceptionType((arg) -> () -> I18n.getInstance().translateKeyAndFormat("commands.commander.playsound.exception_failure", arg));

    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("playsound")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, String>argument("sound", SoundTypeArgumentType.soundType())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, SoundCategory>argument("category", SoundCategoryArgumentType.soundCategory())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, DoubleCoordinates>argument("position", Vec3dArgumentType.vec3d())
                                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Float>argument("volume", FloatArgumentType.floatArg(0.0f))
                                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Float>argument("pitch", FloatArgumentType.floatArg(0.0f, 2.0f))
                                                        .executes(c -> {
                                                            String sound = c.getArgument("sound", String.class);
                                                            SoundCategory category = c.getArgument("category", SoundCategory.class);
                                                            DoubleCoordinates position = c.getArgument("position", DoubleCoordinates.class);
                                                            float volume = c.getArgument("volume", Float.class);
                                                            float pitch = c.getArgument("pitch", Float.class);

                                                            if (c.getSource().playSound(sound, category, (float) position.getX(c.getSource()), (float) position.getY(c.getSource(), true), (float) position.getZ(c.getSource()), volume, pitch)) {
                                                                c.getSource().sendTranslatableMessage("commands.commander.playsound.success", sound, category);
                                                                return Command.SINGLE_SUCCESS;
                                                            } else {
                                                                throw FAILURE.create(sound);
                                                            }
                                                        })))))));
    }
}
