package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {
    private Socket clientSocket;
    private String username;
    private String serverName;
    private PacketStream ps;
    private List<String> idQueue;
    private List<String> chatList;

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
    public int listen() {
            try {
                clientSocket.setSoTimeout(5000);
                Packet packet = Packet.fromByteArray(clientSocket.getInputStream().readAllBytes());
                switch (packet.getOp()) {
                    case QUEUE_UPDATE:
                        QueueUpdatePacket queuePacket = new QueueUpdatePacket(packet);
                        idQueue = queuePacket.getIds();
                        return 0;
                    case CHAT:
                        ChatPacket chatPacket = new ChatPacket(packet);
                        chatList = chatPacket.getMessages();
                        return 1;
                    default:
                        return -1;
                }
            } catch(SocketTimeoutException e) {
                sendTimeoutToGUI();
                return -1;
            } catch(SocketException e) {
                System.out.println("Socket has timed out.");
                return -1;
            } catch(IOException e) {
                System.out.println("IO Exception has occurred.");
                return -1;
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
//    public void sendChatMessage(String message) {
//        try {
//            SyncedTime syncedTime = new SyncedTime(serverName);
//            MessagePacket packet = new MessagePacket(syncedTime.getTime(), message);
//            clientSocket.getOutputStream().write(packet.toByteArray());
//        } catch(IOException e) {
//            System.out.println("IO Exception has occurred.");
//        }
//    }


    /**
     * This function tells the GUI that the client has received a timeout exception.
     * @return A string that says you have lost connection with the server.
     */
    public String sendTimeoutToGUI() { return "You have lost connection with the Server"; }

    /**
     * This method send the queue of video URLs to the GUI to be displayed
     * @return The queue list as a list of strings
     */
    public List<String> sendQueueToGUI() {
        if(idQueue != null) {
            return idQueue;
        } else {
            List<String> noQueueList = new ArrayList<>();
            noQueueList.add("Queue has not been initialized");
            return noQueueList;
        }
    }

    /**
     * This method sends the chat to the GUI for viewing by the user
     * @return The chat as a list of Strings.
     */
    public List<String> sendChatToGUI() { return chatList; }
}
