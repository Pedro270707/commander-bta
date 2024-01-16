package net.pedroricardo.commander;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.content.RequestCommandManagerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.NetworkHelper;

public class Commander implements ModInitializer {
    public static final String MOD_ID = "commander";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int maxSuggestions = 10;
    public static boolean suggestionsFollowParameters = true;

    public static String ENTITY_PREFIX = "entity.";

    public static JsonObject serverSuggestions = new JsonObject();

    @Override
    public void onInitialize() {
        LOGGER.info("Commander initialized.");
        CommanderHelper.init();
        NetworkHelper.register(CommandManagerPacket.class, false, true);
        NetworkHelper.register(RequestCommandManagerPacket.class, true, false);
    }
}
