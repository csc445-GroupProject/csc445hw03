package edu.oswego.cs.ytsync.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayDeque;

public class PacketStream {
    private ArrayDeque<Packet> bufferedPackets;
    private DataInputStream in;
    private ByteBuffer buffer;

    public PacketStream(DataInputStream in) {
        this.in = in;
        buffer = ByteBuffer.allocate(32768);
        bufferedPackets = new ArrayDeque<>();
    }

    public void bufferPackets() throws IOException {
        byte[] readBytes = new byte[16384];
        int remaining = in.read(readBytes);
        buffer.put(readBytes);
        while (remaining > 4) {
            int size = buffer.getInt(0);
            if (buffer.remaining() >= size) {
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

    public boolean hasNext() {
        return !bufferedPackets.isEmpty();
    }

    public Packet next() {
        return bufferedPackets.poll();
    }
}
