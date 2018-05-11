package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;

import edu.oswego.cs.ytsync.client.Client;
import edu.oswego.cs.ytsync.common.*;
import edu.oswego.cs.ytsync.common.raft.RaftMessage;

public class Server extends Thread{
	private final static int PORT = 2706;
	
	public static void main(String args[]) throws IOException {
		ServerSocket host = new ServerSocket(PORT);
		Set<Socket> clients = new CopyOnWriteArraySet<>();
		int clientCount = clients.size();
		System.out.printf("Listening on port %d.\n", host.getLocalPort());

		new Thread(() -> {
		    try {
		        while (true) {
                    Socket client = host.accept();
                    System.out.printf("New client %s.\n", client.getInetAddress());
                    clients.add(client);
                }
            } catch (IOException e) {
		        throw new RuntimeException();
            }
		}).start();

		while (true) {
		    if(clientCount != clients.size()) {
		        System.out.printf("Sending client list\n");

		        clientCount = clients.size();

		        List<String> clientList = new ArrayList<>();

		        for(Socket s : clients) {
		            clientList.add(s.getInetAddress().toString());
                }

                for (Socket s : clients) {
                    s.getOutputStream().write(RaftMessage.hostnameList(clientList).toByteArray());
                    s.getOutputStream().flush();
                }
            }
        }
	}
}
