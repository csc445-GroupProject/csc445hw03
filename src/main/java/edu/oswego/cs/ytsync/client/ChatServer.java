package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.*;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.control.TextArea;

public class ChatServer implements Runnable{
    private SyncedTime syncedTime;
    private List<String> hostnames;
    private List<String> chat;
    private String messageToBeSent;
    private int numOfUsers;
    private int majority;
    private int votedFor;
    private State state;
    private ServerSocket chatSocket;
    private Socket serverSocket;
    private String address;
    private String serverName;
    private int port;
    private int chatPort;
    private int currentTerm;
    private AtomicBoolean hostnamesLocked;
    private AtomicBoolean chatLocked;
    private boolean voted = false;
    private TextArea guiTextArea;

    /**
     * Constructor for the Chat Server
     * @param serverName the hostname for the main server for the application
     * @param addr the hostname of the client that is associated with this particular node
     * @param serverPort the port number for the port associated with the main server for the application
     * @param chatPort the port number that this chat server will listen for incoming messages from
     * @param hostnames a list of messages sent from the main server for the application
     * @param guiTextArea the text area that the server will append its changes to
     */
    ChatServer(String serverName, String addr, int serverPort, int chatPort, List<String> hostnames, TextArea guiTextArea) {
        this.serverName = serverName;
        currentTerm = 0;
        address = addr;
        this.port = serverPort;
        this.hostnames = hostnames;
        numOfUsers = hostnames.size();
        majority = numOfUsers/2;
        state = State.FOLLOWER;
        chat = new ArrayList<>();
        hostnamesLocked = new AtomicBoolean(false);
        chatLocked = new AtomicBoolean(false);
        syncedTime = new SyncedTime(serverName);

        //opens the necessary Sockets
        try{
            serverSocket = new Socket(address, port); //the socket to communicate with the program's main server
            this.chatPort = chatPort;
            chatSocket = new ServerSocket(this.chatPort); //This socket is created for use when this client is in the LEADER state
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to get the updated list of hostnames from the main server of the application
     * @param updatedList the updated list of hostnames
     */
    public void getUpdatedClientListFromServer(List<String> updatedList) {
        boolean updated = false;
        while(!updated) {
            if(hostnamesLocked.compareAndSet(false, true)) {
                hostnames = updatedList;
                updated = true;
                hostnamesLocked.compareAndSet(true, false);
            }
        }
    }

    /**
     * a method that will commit the message to the chat area in the GUI after the majority of clients have received
     * the message
     * @param message the message to be committed
     */
    public void appendEntry(String message) { guiTextArea.appendText(message); }


    public void getMessage(String message) {
        boolean sent = false;
        List<String> tempChat = chat;
        tempChat.add(message);
        while(!sent) {
            if (state == State.LEADER) {
                appendEntry(message);
                //TODO send latest log entry to other clients
                //TODO wait for majority of other clients to acknowledge the log
                //TODO commit the change with CompareAndSet of hostnamesLocked
                //TODO notify other clients that the change has been committed
            } else if (state == State.FOLLOWER) {

            }
        }
    }

    /**
     * This method is performed when a socket timeout occurs while the server is waiting to hear from the Leader Server
     * The server will wait until a majority of other nodes have voted for the current node to change its state to leader
     * and begin broadcasting its heartbeat to the other clients
     */
    public void runForLeader() { //TODO implementation is flawed
        currentTerm++;
        state = State.CANDIDATE;
        boolean successful = false;

        while(!successful) {
            if(hostnamesLocked.compareAndSet(false, true)) {
                successful = true;
                int clientSocketNumber = 65500;
                Random r = new Random();
                int timeoutTime = r.nextInt(150) + 150;
                for(String hostname : hostnames) { //make threads for each of these to run concurrently
                    try {
                        Socket socket = new Socket(hostname, clientSocketNumber);
                        clientSocketNumber--;
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        SyncedTime syncedTime = new SyncedTime(serverName);
                        LeaderVotePacket packet = new LeaderVotePacket(syncedTime.getTime(), address, currentTerm);
                        out.write(packet.toByteArray());
                        out.flush();
                        socket.setSoTimeout(timeoutTime);
                        Packet receivedPacket = Packet.fromByteArray(socket.getInputStream().readAllBytes());
                        if(receivedPacket.getOp() == Opcode.VOTE) {
                            LeaderVotePacket leaderVotePacket = new LeaderVotePacket(receivedPacket);
                            if(leaderVotePacket.getHostname().equals(address)) {
                                votedFor++;
                            }
                        }
                    } catch(SocketTimeoutException e) {
                        System.out.println("SocketTimeout");
                        hostnamesLocked.compareAndSet(true, false);
                    }catch(IOException e) {
                        System.out.println("Unable to open the socket for " + hostname);
                        hostnamesLocked.compareAndSet(true, false);
                    }
                }
                hostnamesLocked.compareAndSet(true, false);

            }
        }
        if(votedFor>majority) {
            state = State.LEADER;
        }
    }

    /**
     * This method sends the nodes vote for the leader to the node who first sent the Leader request
     * @param packet the packet the node received with the hostname of the requesting node
     * @param socket the socket the node used to communicate with this node.
     */
    public void sendVoteForLeader(LeaderVotePacket packet, Socket socket) {
        Random r = new Random();
        int socketTimeoutTime = r.nextInt(150) + 150;

        try {
            socket.setSoTimeout(socketTimeoutTime);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(packet.toByteArray());
            out.flush();
        } catch(SocketTimeoutException e) {
            runForLeader();
        } catch(SocketException e) {
            runForLeader();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {

    }

    /**
     * The method that the node uses to listen for incoming packets
     */
    @Override
    public void run() {
        while(true) {
            //TODO establish a heartbeat connection between Leader and Followers based on current state
            //if leader
            if (state == State.LEADER) {
                boolean chatSent = false;
                while (!chatSent) {
                    if (chatLocked.compareAndSet(false, true)) {
                        int socketNumber = 65500;
                        for (String hostname : hostnames) { // create threads for each of these
                            try {
                                Socket socket = new Socket(hostname, socketNumber);
                                SyncedTime syncedTime = new SyncedTime(serverName);
                                long timeStamp = syncedTime.getTime();
                                ChatPacket packet = new ChatPacket(timeStamp, chat);
                                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                out.write(packet.toByteArray());
                                DataInputStream in = new DataInputStream(socket.getInputStream());
                                Packet p = Packet.fromByteArray(in.readAllBytes());

                                switch (p.getOp()) {
                                    case CHAT:

                                }
                            } catch (SocketException e) {
                                System.out.println("An error occurred with the socket.");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } else if (state == State.FOLLOWER) { //if follower


                //TODO if timeout occurs set state to CANDIDATE AND conduct leader elections
            }


        }
    }


}
