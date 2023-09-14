package net.pedroricardo.commander;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.net.command.CommandHandler;
import net.minecraft.core.net.command.CommandSender;
import net.pedroricardo.commander.commands.CommanderCommand;
import net.pedroricardo.commander.commands.CommandParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.CommandHelper;


public class Commander implements ModInitializer {
    public static final String MOD_ID = "commander";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int maxSuggestions = 4;
    public static boolean suggestionsFollowParameters = true;

    @Override
    public void onInitialize() {
        LOGGER.info("Commander initialized.");
        CommandHelper.createCommand(new CommanderCommand("testCommand1") {
            @Override
            public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] strings) {
                return false;
            }

            @Override
            public boolean opRequired(String[] strings) {
                return false;
            }

            @Override
            public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {

            }
        }.withParameter(CommandParameters.FLOAT_COORDINATES));
        CommandHelper.createCommand(new CommanderCommand("testCommand2") {
            @Override
            public boolean execute(CommandHandler commandHandler, CommandSender commandSender, String[] strings) {
                return false;
            }

            @Override
            public boolean opRequired(String[] strings) {
                return false;
            }

            @Override
            public void sendCommandSyntax(CommandHandler commandHandler, CommandSender commandSender) {

            }
        }.withParameter(CommandParameters.INTEGER_COORDINATES));
    }
}
