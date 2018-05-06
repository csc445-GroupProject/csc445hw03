package edu.oswego.cs.ytsync.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayDeque;

public class PacketStream {
    private ArrayDeque<Packet> bufferedPackets;
    private DataInputStream in;
    private byte[] buff;
    private ByteBuffer buffer;
    private int index;

    public PacketStream(DataInputStream in) {
        this.in = in;
        buff = new byte[32768];
        buffer = ByteBuffer.wrap(buff);
        bufferedPackets = new ArrayDeque<>();
    }

    public void bufferPackets() {
        try {
            if(in.available() > 0) {
                int remaining = in.read(buff, buffer.position(), buff.length - buffer.position());
                while (remaining > 4) {
                    int size = buffer.getInt(0);
                    if (remaining >= size) {
                        buffer.position(0);
                        byte[] packetArray = new byte[size];
                        buffer.get(packetArray);
                        buffer.compact();
                        buffer.position(0);
                        remaining -= size;

                        bufferedPackets.add(Packet.fromByteArray(packetArray));
                    } else {
                        buffer.position(remaining);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasNext() {
        return !bufferedPackets.isEmpty();
    }

    public Packet next() {
        return bufferedPackets.poll();
    }
}
