package net.pedroricardo.commander;

import com.google.gson.JsonObject;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.net.command.TextFormatting;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.content.RequestCommandManagerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

public class Commander implements ModInitializer, DedicatedServerModInitializer {
    public static final String MOD_ID = "commander";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int maxSuggestions = 10;
    public static boolean suggestionsFollowParameters = true;

    public static String ENTITY_PREFIX = "entity.";

    public static JsonObject serverSuggestions = CommanderHelper.getDefaultServerSuggestions();

    public static final List<String> ARGUMENT_STYLES = new ArrayList<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Commander initialized.");
        CommanderHelper.init();
        ARGUMENT_STYLES.add(TextFormatting.LIGHT_BLUE.toString());
        ARGUMENT_STYLES.add(TextFormatting.YELLOW.toString());
        ARGUMENT_STYLES.add(TextFormatting.LIME.toString());
        ARGUMENT_STYLES.add(TextFormatting.PINK.toString());
        ARGUMENT_STYLES.add(TextFormatting.ORANGE.toString());
        NetworkHelper.register(CommandManagerPacket.class, false, true);
        NetworkHelper.register(RequestCommandManagerPacket.class, true, false);
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Commander is installed on the server. Packets with suggestions will be sent to clients with Commander. The lower the packet delay is, the faster packets will be sent to the client, so it will be less laggy for them.");
    }
}
