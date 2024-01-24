package net.pedroricardo.commander;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.net.command.TextFormatting;
import net.pedroricardo.commander.content.CommandManagerPacket;
import net.pedroricardo.commander.content.RequestCommandManagerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.helper.NetworkHelper;
import turniplabs.halplibe.util.GameStartEntrypoint;

import java.text.ParseException;
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
//        try {
//            System.out.println(NbtHelper.parseNbt("{Data1:16,Data2:\"hello\",Data3:[D;1.52,940.2,38.3],Data4:[B;1b,12b]}"));
//            System.out.println(NbtHelper.parseNbt("{DataShort:23s,DataInt:39230,DataString:\"Hello!\"}"));
//            System.out.println(NbtHelper.parseNbt("{DataBoolTrue:true,DataBoolFalse:false,DataByte:22b, DataShort : 51s, DataInt:42351, DataLong: 2934021941L, DataFloat: 3.1415926f, DataDouble: 22.543d, DataDoubleNoSuffix: 2942.43290}"));
//            System.out.println(NbtHelper.parseNbt("{DataByteArray:[B;6b,12b,24b] ,DataShortArray:[S ; 20s , 40s , 63s ] , DataDoubleArray:[D ; 22.53, 239.43d, 39.4D]}"));
//            System.out.println(NbtHelper.parseNbt("{DataList:[6b,12b,24b]}"));
//        } catch (CommandSyntaxException e) {
//            System.out.println(e.getMessage());
//        }
    }

    @Override
    public void onInitializeServer() {
        LOGGER.info("Commander is installed on the server. Packets with suggestions will be sent to clients with Commander. The lower the packet delay is, the faster packets will be sent to the client, so it will be less laggy for them.");
    }
}
