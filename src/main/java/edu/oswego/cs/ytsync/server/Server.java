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

	public static void main(String args[]) throws IOException {
		ServerSocket host = new ServerSocket(PORT);
		System.out.printf("Listening on PORT %d.\n", host.getLocalPort());

		while (true) {
			//Client first connects to the server
			Socket client = host.accept();
			System.out.printf("New connection from %s:%d\n", client.getInetAddress().getHostAddress(), client.getPort());

			PacketStream ps = new PacketStream(new DataInputStream(client.getInputStream()));
			while(!ps.hasNext()) {
				ps.bufferPackets();
			}
			Packet recv = ps.next();
			client.getOutputStream().write(recv.toByteArray());
			client.getOutputStream().flush();
		}
	}

}
