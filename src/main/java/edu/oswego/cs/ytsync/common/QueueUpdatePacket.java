package edu.oswego.cs.ytsync.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class QueueUpdatePacket extends Packet{
    public QueueUpdatePacket(long timestamp, List<String> ids) {
        super(Opcode.QUEUE_UPDATE, timestamp);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream payloadOutputStream = new DataOutputStream(byteOutputStream);

        try {
            payloadOutputStream.writeInt(ids.size());

            for(String id : ids) {
                payloadOutputStream.writeUTF(id);
            }

            setPayload(byteOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<String> getIds() throws IOException{
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(getPayload());
        DataInputStream listInputStream = new DataInputStream(byteInputStream);

        int size = listInputStream.readInt();

        ArrayList<String> ids = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
            ids.add(listInputStream.readUTF());
        return ids;
    }
}
