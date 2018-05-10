package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.LeaderVotePacket;
import edu.oswego.cs.ytsync.common.Opcode;
import edu.oswego.cs.ytsync.common.Packet;
import edu.oswego.cs.ytsync.common.SyncedTime;

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

public class ChatServer implements Runnable{
    private List<String> hostnames;
    private List<String> chat;
    private List<List<String>> log;
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

    ChatServer(String serverName, String addr, int serverPort, int chatPort, List<String> hostnames) {
        this.serverName = serverName;
        currentTerm = 0;
        address = addr;
        this.port = serverPort;
        this.hostnames = hostnames;
        numOfUsers = hostnames.size();
        majority = numOfUsers/2;
        state = State.FOLLOWER;
        chat = new ArrayList<>();
        log = new ArrayList<>();
        hostnamesLocked = new AtomicBoolean(false);
        chatLocked = new AtomicBoolean(false);

        //opens the necessary Sockets
        try{
            serverSocket = new Socket(address, port); //the socket to communicate with the program's main server
            this.chatPort = chatPort;
            chatSocket = new ServerSocket(this.chatPort); //This socket is created for use when this client is in the LEADER state
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

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

    public void setChat(List<String> chat) {
        this.chat = chat;
    }

    public void getMessage(String message) {
        boolean sent = false;
        List<String> tempChat = chat;
        tempChat.add(message);
        while(!sent) {
            if (state == State.LEADER) {
                log.add(chat);
                //TODO send latest log entry to other clients
                //TODO wait for majority of other clients to acknowledge the log
                //TODO commit the change with CompareAndSet of hostnamesLocked
                //TODO notify other clients that the change has been committed
            } else if (state == State.FOLLOWER) {

            }
        }
    }

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
                for(String hostname : hostnames) {
                    try {
                        Socket socket = new Socket(hostname, clientSocketNumber);
                        clientSocketNumber--;
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        SyncedTime syncedTime = new SyncedTime(serverName);
                        syncedTime.updateTime();
                        LeaderVotePacket packet = new LeaderVotePacket(syncedTime.getTime(), address);
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

    public void sendVoteForLeader(LeaderVotePacket packet, Socket socket) {
        Random r = new Random();
        int socketTimeoutTime = r.nextInt(150) + 150;

        try {
            socket.setSoTimeout(socketTimeoutTime);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(packet.toByteArray());
        } catch(SocketTimeoutException e) {
            runForLeader();
        } catch(SocketException e) {
            runForLeader();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //TODO establish a heartbeat connection between Leader and Followers based on current state
        //if leader

        //if follower

        //TODO if timeout occurs set state to CANDIDATE AND conduct leader elections
    }


}
