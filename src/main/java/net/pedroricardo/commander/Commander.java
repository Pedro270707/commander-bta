package net.pedroricardo.commander;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.net.packet.Packet138PlayerList;
import net.pedroricardo.commander.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commander implements ModInitializer {
    public static final String MOD_ID = "commander";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static int maxSuggestions = 10;
    public static boolean suggestionsFollowParameters = true;

    public static String ENTITY_PREFIX = "entity.";

    @Override
    public void onInitialize() {
        LOGGER.info("Commander initialized.");
        CommanderHelper.init();
    }
}
