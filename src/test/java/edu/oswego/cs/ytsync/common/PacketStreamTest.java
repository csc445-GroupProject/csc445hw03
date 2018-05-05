package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PacketStreamTest {

    @Test
    public void testStream() throws IOException {
        ServerSocket server = new ServerSocket(0);
        Socket client = new Socket(server.getInetAddress(), server.getLocalPort());
        client.setSoTimeout(500);
        Socket host = server.accept();

        List<Packet> sendPackets = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            sendPackets.add(new Packet(Opcode.CONNECT, System.nanoTime(), "hello".getBytes()));
            host.getOutputStream().write(sendPackets.get(i).toByteArray());
            host.getOutputStream().flush();
        }

        List<Packet> recvPackets = new ArrayList<>();
        PacketStream ps = new PacketStream(new DataInputStream(client.getInputStream()));
        for(int i = 0; i < 4; i++) {
            recvPackets.add(ps.next());
        }

        assertEquals(sendPackets, recvPackets);
    }
}