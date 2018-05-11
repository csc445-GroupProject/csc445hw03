package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PacketStreamTest {

    @Test
    public void testStream() throws IOException, InterruptedException {
        ServerSocket server = new ServerSocket(0);
        Socket client = new Socket(server.getInetAddress(), server.getLocalPort());
        client.setSoTimeout(500);
        Socket host = server.accept();

        List<Packet> sendPackets = new ArrayList<>();
        Thread sendThread = new Thread(() -> {
            try {
                for (int i = 0; i < 100000; i++) {
                    sendPackets.add(new Packet(Opcode.CONNECT, System.nanoTime(), "hello".getBytes()));
                    host.getOutputStream().write(sendPackets.get(i).toByteArray());
                    host.getOutputStream().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        List<Packet> recvPackets = new ArrayList<>();
        PacketStream ps = new PacketStream(new DataInputStream(client.getInputStream()));
        Thread recvThread = new Thread(() -> {
           for(int i = 0; i < 100000; i++) {
               while (!ps.hasNext())
                   ps.bufferPackets();

               recvPackets.add(ps.next());
           }
        });

        sendThread.start();
        recvThread.start();
        sendThread.join();
        recvThread.join();


        assertEquals(sendPackets, recvPackets);
    }
}