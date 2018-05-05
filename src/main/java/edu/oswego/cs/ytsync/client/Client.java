package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.ConnectPacket;
import edu.oswego.cs.ytsync.common.Opcode;
import edu.oswego.cs.ytsync.common.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client implements Runnable {
    private Socket clientSocket;
    private String username;

    Client(String addr, int port, String username) throws IOException {
        this.username = username;
        clientSocket = new Socket(addr, port);
    }

    public String getUsername() {
        return username;
    }

    /**
     * This method creates the initial connection with the Server.
     * @return
     */
    public boolean initialConnection() {
        Packet connectionPacket = new Packet(Opcode.CONNECT, System.nanoTime());
        if(username != null) {
            connectionPacket.setPayload(username.getBytes());
        }
        return false;
    }

    @Override
    public void run() {
//        socket.
//        ConnectPacket connectPacket = new ConnectPacket(System.nanoTime(), username);
//        try {
//            clientSocket.getChannel().write()
//            Packet receivedPacket = Packet.fromByteArray(in.readAllBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
