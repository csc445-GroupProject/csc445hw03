package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.SyncPacket;

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
    private String userName;

    Client(String userName){
        userName = this.userName;
        serverName = "pi";
        port = 2706;
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
    }

    /**
     * This method creates the initial connection with the Server.
     * @return
     */
    public boolean initialConnection() {
        SyncPacket connectionPacket = new SyncPacket(SyncPacket.Opcode.JOIN, 0, System.nanoTime());
        if(userName != null) {
            connectionPacket.setPayload(userName.getBytes());
        }
        return false;
    }
}
