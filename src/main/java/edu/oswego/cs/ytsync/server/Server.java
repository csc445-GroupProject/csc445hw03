package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import edu.oswego.cs.ytsync.Client;
import edu.oswego.cs.ytsync.common.*;

public class Server extends Thread{
	private final static int PORT = 2706;
	
	public static void main(String args[]) throws IOException {
		ServerSocket host = new ServerSocket(PORT);
		System.out.println("Listening on PORT %d.\n" + host.getLocalPort() + " address: " + host.getInetAddress()	);
		//List of messages
		ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<>();
		PriorityBlockingQueue<String> pbq = new PriorityBlockingQueue<>();
		List<String> queueList = (List)pbq;
		Thread[] allThreads;
		
		
		int x = 0; //possibly use to name threads
		while (true) {
			x += 1;
			//Client first connects to the server
			Socket clientSocket = host.accept();
			System.out.printf("New connection from %s:%d\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
				try {
						//System.out.println("Thread");
						ClientHandler clientHandler = new ClientHandler(clientSocket, clq, pbq);
						new Thread(clientHandler).run();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				//Need to create way to send packets to all clients
				}
		}

}
