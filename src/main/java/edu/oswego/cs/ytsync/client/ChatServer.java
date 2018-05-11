package edu.oswego.cs.ytsync.client;

import edu.oswego.cs.ytsync.common.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.oswego.cs.ytsync.common.raft.*;
import javafx.scene.control.TextArea;

public class ChatServer implements Runnable{
    private SyncedTime syncedTime;
    private List<String> hostnames;
    private List<Socket> nodeSockets;
    private List<LogEntry> log;
    private List<Host> hosts;
    private String messageToBeSent;
    private int numOfUsers;
    private int majority;
    private int votedFor;
    private State state;
    private String address;
    private String serverName;
    private int port;
    private int chatPort;
    private int currentTerm;
    private int electionVotes;
    private int commitVotes;
    private int lastCommitted;
    private AtomicBoolean hostnamesLocked;
    private AtomicBoolean socketsLocked;
    private AtomicBoolean logLocked;
    private AtomicBoolean bufferLocked;
    private boolean voted = false;
    private TextArea guiTextArea;
    private Socket socketForDedicatedServer;
    private Host leader;
    private Host me;
    private RaftMessageBuffer buffer;

    /**
     * Constructor for the Chat Server
     * @param serverName the hostname for the main server for the application
     * @param addr the hostname of the client that is associated with this particular node
     * @param serverPort the port number for the port associated with the main server for the application
     * @param hostnames a list of messages sent from the main server for the application
     * @param guiTextArea the text area that the server will append its changes to
     */
    ChatServer(String serverName, String addr, int serverPort, List<String> hostnames, TextArea guiTextArea) {
        this.serverName = serverName;
        currentTerm = 0;
        address = addr;
        this.port = serverPort;
        this.hostnames = hostnames;
        numOfUsers = hostnames.size();
        majority = numOfUsers/2;
        state = State.FOLLOWER;
        hostnamesLocked = new AtomicBoolean(false);
        bufferLocked = new AtomicBoolean(false);
        socketsLocked = new AtomicBoolean(false);
        logLocked = new AtomicBoolean(false);
        syncedTime = new SyncedTime(serverName);
        nodeSockets = new ArrayList<>();
        this.guiTextArea = guiTextArea;
        log = new ArrayList<>();
        hosts = new ArrayList<>();
        buffer = new RaftMessageBuffer();
        electionVotes = 1;
        commitVotes = 1;
        lastCommitted = 0;
        try {
            socketForDedicatedServer = new Socket(serverName, serverPort);
        } catch(IOException e) {
            System.out.println("Couldn't connect to Server");
        }
    }


    /**
     * Creates a Host object for each client connected to the application
     */
    private void createHostsForHostNames() {
        boolean hostsLocked = false;
        int serverSocketPortNumber = 65500;
        while(!hostsLocked) {
            if(hostnamesLocked.compareAndSet(false, true)) {
                hostsLocked = true;
                for(String hostname : hostnames) {
                    boolean hostFound = false;
                    for(Host host : hosts) {
                        if(host.getHostname().equals(hostname))
                            hostFound = true;
                    }
                    if (!hostFound) {
                        Host newHost = new Host(hostname, serverSocketPortNumber);
                        serverSocketPortNumber--;
                    }
                }
                hostnamesLocked.compareAndSet(true, false);
            }
        }
    }

    /**
     * Opens sockets for all Clients connected to the application. First closes all current sockets to account for any
     * dropped clients, Then opens new Sockets for clients
     */
    private void openSocketsForNewHosts() {
        boolean hostsLocked = false;
        boolean areSocketsLocked = false;
        while(!hostsLocked) {
            if(hostnamesLocked.compareAndSet(false, true)) {
                hostsLocked = true;
                while(!areSocketsLocked) {
                    if (socketsLocked.compareAndSet(false, true)) {
                        areSocketsLocked = true;
                        for (Socket socket : nodeSockets) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        nodeSockets = new ArrayList<>();
                        for(Host host : hosts) {
                            try {
                                nodeSockets.add(new Socket(host.getHostname(), host.getServerSocketPort()));
                            } catch(IOException e) {
                                System.out.println("Couldn't open socket for host: "+ host.getHostname());
                            }
                        }


                        socketsLocked.compareAndSet(true, false);
                    }
                }
                hostnamesLocked.compareAndSet(true, false);
            }
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
                createHostsForHostNames();
                openSocketsForNewHosts();
                updated = true;
                hostnamesLocked.compareAndSet(true, false);
            }
        }
    }

    /**
     * This method is performed when a socket timeout occurs while the server is waiting to hear from the Leader Server
     * The server will wait until a majority of other nodes have voted for the current node to change its state to leader
     * and begin broadcasting its heartbeat to the other clients
     */
    private boolean runForLeader() { //TODO implementation is flawed
        currentTerm++;
        state = State.CANDIDATE;
        boolean successful = false;
        AtomicBoolean bufferLocked = new AtomicBoolean(false);
        boolean hostsLocked = false;

        while(!hostsLocked) {
            if(hostnamesLocked.compareAndSet(false, true)) {
                hostsLocked = true;
                int candidateId = -1;
                for(int i=0, length = hosts.size(); i<length; i++) {
                    if(me.getHostname().equals(hosts.get(i).getHostname())) {
                        candidateId = i;
                    }
                }

                if(candidateId != -1) {
                    Random r = new Random();
                    int timeout = r.nextInt(150) + 150;
                    while (!successful) {
                        if (socketsLocked.compareAndSet(false, true)) {
                            successful = true;
                            for (Socket socket : nodeSockets) {
                                try {
                                    //create thread
                                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                    RaftMessage leaderRequest = RaftMessage.voteRequest(currentTerm, candidateId, log.size() - 1, log.get(log.size() - 1).getTerm());
                                    out.write(leaderRequest.toByteArray());
                                    out.flush();
                                    socket.setSoTimeout(timeout);
                                } catch (IOException e) {
                                    socketsLocked.compareAndSet(true, false);
                                    return false;

                                }
                            }
                        }
                    }
                }
            }
        }
        socketsLocked.compareAndSet(true, false);
        return true;
    }

    /**
     * This method sends the nodes vote for the leader to the node who first sent the Leader request
     * @param message the RaftMessage the node received with the hostname of the requesting node
     * @param socket the socket the node used to communicate with this node.
     */
    private void sendVoteForLeader(RaftMessage message, Socket socket) {
        if(message.getTerm() > currentTerm) {
            currentTerm = message.getTerm();
            RaftMessage response = RaftMessage.voteResponse(currentTerm, true);
            Random r = new Random();
            int socketTimeoutTime = r.nextInt(150) + 150;

            try {
                socket.setSoTimeout(socketTimeoutTime);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(response.toByteArray());
                out.flush();
            } catch (SocketTimeoutException e) {
                runForLeader();
            } catch (SocketException e) {
                runForLeader();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A method to get a new chat message from the Client who owns this chatServer
     * @param message the message that the client is sending
     */
    public void getNewMessageFromConnectedClient(String username, String message) {
        LogEntry newLogEntry = new LogEntry(currentTerm, username, message);
        RaftMessage newMessage = RaftMessage.chatMessage(newLogEntry);
        getNewMessage(newMessage);
    }

    /**
     * adds the newly received RaftMessage to the log to be sent to all the node's peers
     * @param message the RaftMessage that was received
     * @return true if the message was added to the log, false if the addition was unsuccessful
     */
    private boolean getNewMessage(RaftMessage message) {
        if(message.getType() == MessageType.CHAT_MESSAGE) {
            boolean isLogLocked = false;
            while (!isLogLocked) {
                if (logLocked.compareAndSet(false, true)) {
                    isLogLocked = true;
                    log.add(message.getEntry());
                    logLocked.compareAndSet(true, false);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Listens for incoming RaftMessages from a specific peer, designed to be thread safe
     * @param socket the socket to listen from
     */
    public void listen(Socket socket) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] newRaftMessage = in.readAllBytes();
            boolean isBufferLocked = false;

            while(!isBufferLocked) {
                if(bufferLocked.compareAndSet(false, true)) {
                    isBufferLocked = true;
                    buffer.addToBuffer(newRaftMessage, newRaftMessage.length);
                }
            }

        } catch (IOException e) {
            System.out.println("IO Exception has occurred");
        }
    }

    /**
     * Handles the RaftMessages currently in the buffer, will call the appropriate methods depending on the packet type
     */
    private void handleMessages() {
        boolean isBufferLocked = false;
        boolean areSocketsLocked;

        while(!isBufferLocked) {
            if(bufferLocked.compareAndSet(false, true)) {
                isBufferLocked = true;
                if(buffer.hasNext()) {
                    RaftMessage message = buffer.next();

                    switch (message.getType()) {
                        case VOTE_REQUEST:
                            electionVotes = 1;
                            areSocketsLocked = false;
                            while(!areSocketsLocked) {
                                if(socketsLocked.compareAndSet(false, true)) {
                                    areSocketsLocked = true;
                                    Socket socket = nodeSockets.get(message.getLeaderId());
                                    sendVoteForLeader(message, socket);
                                    socketsLocked.compareAndSet(true, false);
                                }
                            }
                            break;

                        case VOTE_RESPONSE:
                            if(state == State.CANDIDATE) {
                                if(message.getVoteGranted()){
                                    updateElectionVoteCount();
                                }
                            }
                            break;

                        case CHAT_MESSAGE:
                            getNewMessage(message);
                            break;

                        case APPEND_REQUEST:
                            areSocketsLocked = false;
                            while(!areSocketsLocked) {
                                if (socketsLocked.compareAndSet(false, true)) {
                                    areSocketsLocked = true;
                                    Socket responseSocket = nodeSockets.get(message.getLeaderId());
                                    respondToAppendRequest(message, responseSocket);
                                    socketsLocked.compareAndSet(true, false);
                                }
                            }
                            break;

                        case APPEND_RESPONSE:
                            if(message.getTerm() == currentTerm) {
                                if(message.getSuccess())
                                    updateCommitVoteCount();
                            }
                            break;
                    }
                }
                bufferLocked.compareAndSet(true, false);
            }
        }
    }

    /**
     * Responds to the leader node's heartbeat
     * @param message The response that will be sent back to the leader node
     * @param socket The socket that is associated with the leader node
     */
    private void respondToAppendRequest(RaftMessage message, Socket socket) {
        boolean isLogLocked = false;

        while(!isLogLocked) {
            if (logLocked.compareAndSet(false, true)) {
                isLogLocked = true;
                log.add(message.getEntry());
                RaftMessage appendResponse = RaftMessage.appendResponse(currentTerm, true);
                try {
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.write(appendResponse.toByteArray());
                } catch(IOException e) {
                    System.out.println("An IOException has occurred.");
                }
            }
        }
    }

    /**
     * Updates the election votes, changes the state of the node it it has gotten majority vote from the other hosts
     */
    private void updateElectionVoteCount() {
        electionVotes++;
        if(electionVotes > majority) {
            state = State.LEADER;
            electionVotes = 1;
        }
    }

    /**
     * Updates the commit vote count, if the node has gotten majority votes, updates lastCommitted to the last log entry
     * who's term is the same as the currentTerm
     */
    private void updateCommitVoteCount() { //we might have an issue with this, specifically not being able to find which log entries to commit, without committing all of them.
        commitVotes++;
        if(commitVotes > majority) {
            for(int i = lastCommitted, length = log.size(); i<length; i++) {
                if (log.get(i).getTerm() == currentTerm) {
                    lastCommitted = i;
                    commitVotes = 1;
                }
            }
        }
    }

    /**
     * Sends the heartbeat through the given socket to be received by another client
     * @param socket the socket where this method will communicate through
     */
    private void sendHeartbeat(Socket socket) {
        int leaderId = -1;
        for(int i=0, length = hosts.size(); i<length; i++) {
            if(hosts.get(i).getHostname().equals(me.getHostname())) {
                leaderId = i;
            }
        }
        boolean isLogLocked = false;
        while(!isLogLocked) {
            if(logLocked.compareAndSet(false, true)) {
                isLogLocked = true;
                List<LogEntry> newLogEntries = new ArrayList<>();
                for(int i = lastCommitted + 1, length = log.size(); i<length; i++) {
                    newLogEntries.add(log.get(i));
                }
                if (leaderId != -1) {
                    RaftMessage appendRequest = RaftMessage.appendRequest(currentTerm, leaderId, lastCommitted, log.get(log.size()-1).getTerm(), newLogEntries, lastCommitted); //TODO fix prevLogIndex (first lastCommitted)
                    try {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.write(appendRequest.toByteArray());
                    } catch(IOException e) {
                        System.out.println("An IO Exception has occurred.");
                    }
                }
            }
        }
    }

    /**
     * The method that the node uses to listen for incoming packets
     */
    @Override
    public void run() {

        new Thread(()-> {
            while(true)
                handleMessages();
        }, "Message Handler").start();

        while(true) {
            boolean areSocketsLocked = false;
            while(!areSocketsLocked) {
                if(socketsLocked.compareAndSet(false, true)) {
                    areSocketsLocked = true;
                    for (Socket socket : nodeSockets) {

                        new Thread(() -> {
                            listen(socket);
                        }).start();
                    }
                    socketsLocked.compareAndSet(true, false);
                }
            }
            areSocketsLocked = false;
            //if leader
            if (state == State.LEADER) {
                while(!areSocketsLocked) {
                    if(socketsLocked.compareAndSet(false, true)) {
                        areSocketsLocked = true;
                        for(Socket socket : nodeSockets) {
                            new Thread(() -> {
                                sendHeartbeat(socket);
                            }).start();
                        }
                    }
                }
            }
        }
    }


}
