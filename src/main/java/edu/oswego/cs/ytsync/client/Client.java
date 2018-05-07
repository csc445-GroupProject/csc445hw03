package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;

public class Client implements Runnable {
    private Socket clientSocket;
    private String username;
    private String serverName;
    private PacketStream ps;

    Client(String addr, int port, String username) throws IOException {
        this.username = username;
        serverName = addr;
        clientSocket = new Socket(serverName, port);
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

    /**
     * This method listens for incoming packets from the server. This method will populate the GUI with the contents of
     * the packet if necessary.
     */
    public void listen() {
        while(true) {
            try {
                clientSocket.setSoTimeout(5000);
                Packet packet = Packet.fromByteArray(clientSocket.getInputStream().readAllBytes());
                switch (packet.getOp()) {
                    case QUEUE_UPDATE:
                        QueueUpdatePacket queuePacket = new QueueUpdatePacket(packet);
                        //List<String> idQueue = queuePacket.getIds(); // CANT FIGURE THIS OUT, WANTED TO ASK ABOUT THIS
                        sendQueueToGUI();
                        break;

                }
            } catch(SocketTimeoutException e) {
                sendTimeoutToGUI();
            } catch(SocketException e) {
                System.out.println("Socket has timed out.");
            } catch(IOException e) {
                System.out.println("IO Exception has occurred.");
            }
        }
    }

    /**
     * This method sends the video id to the server along with the timestamp of when
     * @param id the youtube id of the video to be added to the playlist.
     */
    public void addVideoToPlaylist(String id) {
        try {
            SyncedTime syncedTime = new SyncedTime(serverName);
            AddQueuePacket packet = new AddQueuePacket(syncedTime.getTime(), id);
            clientSocket.getOutputStream().write(packet.toByteArray());
        } catch(IOException e) {
            System.out.println("IO Exception has occurred");
        }
    }


    /**
     * This method sends a chat message to the server to be synced with all the other messages from other clients.
     * This method sends a MessagePacket to the server through the client socket.
     * @param message A String representing the chat message the client is sending to the server
     */
    public void sendChatMessage(String message) {
        try {
            SyncedTime syncedTime = new SyncedTime(serverName);
            MessagePacket packet = new MessagePacket(syncedTime.getTime(), message);
            clientSocket.getOutputStream().write(packet.toByteArray());
        } catch(IOException e) {
            System.out.println("IO Exception has occurred.");
        }
    }


    /**
     * This function tells the GUI that the client has received a timeout exception
     * THIS FUNCTION STILL NEEDS TO BE EDITED TO RETURN HOWEVER THE TIMEOUT WILL BE SENT TO THE GUI
     */
    public void sendTimeoutToGUI() {

    }

    /**
     * This method sends the new updated queue to the GUI.
     *
     * THIS METHOD NEEDS TO BE UPDATED TO RETURN WHATEVER TYPE OF OBJECT THE QUEUE IS
     */
    public void sendQueueToGUI() {

    }
}
