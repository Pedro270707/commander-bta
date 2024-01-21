package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.util.helper.DamageType;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.DamageTypeArgumentType;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.util.List;

public class DamageCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("damage")
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("entities", EntityArgumentType.entities())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, DamageType>argument("type", DamageTypeArgumentType.damageType())
                                .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("amount", IntegerArgumentType.integer(0, 32768))
                                        .executes(c -> {
                                            List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get(c.getSource());
                                            DamageType type = c.getArgument("type", DamageType.class);
                                            int amount = c.getArgument("amount", Integer.class);
                                            int entitiesAffected = 0;
                                            for (Entity entity : entities) {
                                                if (entity.hurt(null, amount, type)) ++entitiesAffected;
                                            }
                                            c.getSource().sendTranslatableMessage("commands.commander.damage.success_" + (entitiesAffected == 1 ? "single" : "multiple"), entitiesAffected);
                                            return entitiesAffected;
                                        })))));
    }
}
