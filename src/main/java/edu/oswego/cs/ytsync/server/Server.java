package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import edu.oswego.cs.ytsync.common.*;

public class Server {
	private final static int PORT = 2706;
	private static List<Room> rooms;

	public static void main(String args[]) throws IOException {
	    rooms = new ArrayList<>();

		ServerSocket host = new ServerSocket(PORT);
		System.out.printf("Listening on PORT %d.\n", host.getLocalPort());

		while (true) {
			//Client first connects to the server
			Socket client = host.accept();
			System.out.printf("New connection from %s:%d\n", client.getInetAddress().getHostAddress(), client.getPort());
		}
	}

}
