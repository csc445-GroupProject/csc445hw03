package edu.oswego.cs.ytsync.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChatPacket extends Packet {
    public ChatPacket(long timestamp, List<String> chat) {
        super(Opcode.CHAT, timestamp);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream payloadOutputStream = new DataOutputStream(byteOutputStream);

        try {
            payloadOutputStream.writeInt(chat.size());

            for(String message : chat) {
                payloadOutputStream.writeUTF(message);
            }

            setPayload(byteOutputStream.toByteArray());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ChatPacket(Packet p) {
        super(p.getOp(), p.getTimestamp(), p.getPayload());
    }

    public List<String> getMessages() throws IOException {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(getPayload());
        DataInputStream chatInputStream = new DataInputStream(byteInputStream);

        int size = chatInputStream.readInt();

        ArrayList<String> chat = new ArrayList<>(size);

        for(int i=0; i<size; ++i)
            chat.add(chatInputStream.readUTF());
        return chat;
    }
}
