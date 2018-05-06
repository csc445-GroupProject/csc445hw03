package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.ConnectPacket;
import edu.oswego.cs.ytsync.common.Opcode;
import edu.oswego.cs.ytsync.common.Packet;
import edu.oswego.cs.ytsync.common.PacketStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client implements Runnable {
    private Socket clientSocket;
    private String username;
    private PacketStream ps;

    Client(String addr, int port, String username) throws IOException {
        this.username = username;
        clientSocket = new Socket(addr, port);
        ps = new PacketStream(new DataInputStream(clientSocket.getInputStream()));
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
        ConnectPacket connectPacket = new ConnectPacket(System.nanoTime(), username);
        try {
            clientSocket.getOutputStream().write(connectPacket.toByteArray());
            clientSocket.getOutputStream().flush();
            while(!ps.hasNext()) {
                ps.bufferPackets();
            }
            Packet receivedPacket = ps.next();
            if(receivedPacket.getOp() == Opcode.CONNECT) {
                System.out.println(new ConnectPacket(receivedPacket).getUsername());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
