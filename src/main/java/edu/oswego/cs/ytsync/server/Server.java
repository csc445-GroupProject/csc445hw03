package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import edu.oswego.cs.ytsync.client.Client;
import edu.oswego.cs.ytsync.common.*;

public class Server extends Thread{
	private final static int PORT = 2706;
	
	public static void main(String args[]) throws IOException {
		ServerSocket host = new ServerSocket(PORT);
		System.out.println("Listening on PORT %d.\n" + host.getLocalPort() + " address: " + host.getInetAddress()	);
		
		int x = 0;
		while (true) {
			x += 1;
			//Client first connects to the server
			Socket clientSocket = host.accept();
			System.out.printf("New connection from %s:%d\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
				try {
						//System.out.println("Thread");
						ClientHandler clientHandler = new ClientHandler(clientSocket);
						new Thread(clientHandler).run();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}

}
