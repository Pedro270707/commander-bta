package net.pedroricardo.commander.content;

import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.core.net.packet.Packet;
import net.pedroricardo.commander.duck.RequestCommandManagerPacketHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RequestCommandManagerPacket extends Packet {
    public String username;
    public String text;
    public int cursor;

    public RequestCommandManagerPacket(String username, String text, int cursor) {
        this.username = username;
        this.text = text;
        this.cursor = cursor;
    }

    public RequestCommandManagerPacket() {
    }

    @Override
    public void readPacketData(DataInputStream dataInputStream) throws IOException {
        this.username = dataInputStream.readUTF();
        this.text = dataInputStream.readUTF();
        this.cursor = dataInputStream.readInt();
    }

    @Override
    public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(this.username);
        dataOutputStream.writeUTF(this.text);
        dataOutputStream.writeInt(this.cursor);
    }

    @Override
    public void processPacket(NetHandler netHandler) {
        ((RequestCommandManagerPacketHandler)netHandler).commander$handleRequestCommandManagerPacket(this);
    }

    @Override
    public int getPacketSize() {
        return 10;
    }
}
