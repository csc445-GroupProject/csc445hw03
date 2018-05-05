package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.Opcode;
import edu.oswego.cs.ytsync.common.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private Socket connectionSocket;
    private Socket communicationSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String serverName;
    private int port;
    private String username;

    Client(String username){
        this.username = username;
        serverName = "pi";
        port = 2706;
    }

    public void connect() {
        try {
            connectionSocket = new Socket(serverName, port);
        } catch(IOException e) {
            System.out.println("Socket could Not be opened");
        }
        try {
            in = new DataInputStream(connectionSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Input Stream was not instantiated");
        }
    };

    public String getUsername() {
        return username;
    }

    /**
     * This method creates the initial connection with the Server.
     * @return
     */
    public boolean initialConnection() {
        Packet connectionPacket = new Packet(Opcode.JOIN, System.nanoTime());
        if(username != null) {
            connectionPacket.setPayload(username.getBytes());
        }
        return false;
    }
}
