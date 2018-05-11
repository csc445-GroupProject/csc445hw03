package edu.oswego.cs.ytsync.common.raft;

import edu.oswego.cs.ytsync.common.Opcode;
import edu.oswego.cs.ytsync.common.Packet;
import edu.oswego.cs.ytsync.common.PacketStream;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RaftMessageBufferTest {

    @Test
    public void testBuffer() throws IOException, InterruptedException {
        ServerSocket server = new ServerSocket(0);
        Socket client = new Socket(server.getInetAddress(), server.getLocalPort());
        client.setSoTimeout(500);
        Socket host = server.accept();

        List<RaftMessage> sendMessages = new ArrayList<>();
        Thread sendThread = new Thread(() -> {
            try {
                for (int i = 0; i < 10000; i++) {
                    sendMessages.add(RaftMessage.voteRequest(i, i/2, i/3, i/4));
                    host.getOutputStream().write(sendMessages.get(i).toByteArray());
                    host.getOutputStream().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        List<Packet> recvPackets = new ArrayList<>();
        Thread recvThread = new Thread(() -> {
            try {
                RaftMessageBuffer buffer = new RaftMessageBuffer();
                InputStream in = client.getInputStream();

                while (recvPackets.size() < 10000) {
                    if (in.available() > 0) {
                        buffer.addToBuffer(in.readAllBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        sendThread.start();
        recvThread.start();
        sendThread.join();
        recvThread.join();


        assertEquals(sendMessages, recvPackets);
    }
}