package net.pedroricardo.commander.content;

import net.minecraft.client.Minecraft;
import net.minecraft.core.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommanderClientCommandSource implements CommanderCommandSource {
    private final Minecraft mc;

    public CommanderClientCommandSource(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public Collection<String> getPlayerNames() {
        List<String> list = new ArrayList<>();
        for (EntityPlayer player : this.mc.theWorld.players) {
            list.add(player.username);
        }
        return list;
    }

    @Override
    public String getType() {
        return "client";
    }

    @Override
    public EntityPlayer getSender() {
        return this.mc.thePlayer;
    }

    @Override
    public boolean hasAdmin() {
        return true;
    }
}
