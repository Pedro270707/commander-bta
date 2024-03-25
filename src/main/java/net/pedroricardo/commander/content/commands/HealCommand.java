package net.pedroricardo.commander.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.pedroricardo.commander.content.CommanderCommandSource;
import net.pedroricardo.commander.content.arguments.EntityArgumentType;
import net.pedroricardo.commander.content.helpers.EntitySelector;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HealCommand {
    public static void register(CommandDispatcher<CommanderCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<CommanderCommandSource>literal("heal")
                .requires(CommanderCommandSource::hasAdmin)
                .then(RequiredArgumentBuilder.<CommanderCommandSource, EntitySelector>argument("entities", EntityArgumentType.entities())
                        .then(RequiredArgumentBuilder.<CommanderCommandSource, Integer>argument("amount", IntegerArgumentType.integer(0, 32768))
                                .executes(c -> {
                                    List<? extends Entity> entities = c.getArgument("entities", EntitySelector.class).get(c.getSource());
                                    int amount = c.getArgument("amount", Integer.class);

                                    int entitiesAffected = 0;
                                    for (Entity entity : entities) {
                                        if (entity instanceof EntityLiving) {
                                            int maxHealth = ((EntityLiving) entity).getMaxHealth();
                                            int originalHealth = ((EntityLiving) entity).getHealth();
                                            ((EntityLiving)entity).setHealthRaw(MathHelper.clamp(((EntityLiving)entity).getHealth() + amount, 0, maxHealth));
                                            if (((EntityLiving)entity).getHealth() != originalHealth) ++entitiesAffected;
                                        }
                                    }
                                    c.getSource().sendTranslatableMessage("commands.commander.heal.success_" + (entitiesAffected == 1 ? "single" : "multiple"), entitiesAffected);
                                    return entitiesAffected;
                                }))));
    }
}
