package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.oswego.cs.ytsync.common.*;
import org.apache.commons.lang3.ArrayUtils;

public class Server {
	private static boolean verbose;
	private final static int port = 2706;
	private static List<String> videoQueue;

	public static void main(String args[]) throws IOException {
		verbose = ArrayUtils.contains(args, "-verbose");

		ServerSocket host = new ServerSocket(port);
		if(verbose) {
			System.out.printf("Bound on port %d.\n", host.getLocalPort());
		}
		int clients = 0;
		int rooms = 0;
		long timestamp;
		videoQueue = new ArrayList<>();
		while (true) {
			//Client first connects to the server
			Socket client = host.accept();
			if(verbose) {
				System.out.printf("New client %s:%d\n", client.getInetAddress().getHostAddress(), client.getPort());
			}
			//Open up a room with potential for more rooms later
			if(rooms == 0) {
				rooms += 1;
				final int roomNumber = rooms;
				new Thread(() -> { 
					try {
						ClientRoomHandler clientRoom = new ClientRoomHandler(roomNumber);
					} catch (IOException e) {
						System.out.println("Room not created");
						e.printStackTrace();
					}
				},"Room: " + rooms).start();
			}
			
			//Room thread has started
			//get client/server data stream
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());
			new Thread(() -> {
				ClientHandler conClient = new ClientHandler(client, dis, dout);
			}, "Client: " + clients).start();
			timestamp = System.nanoTime();
			
			// Send initial video queue
			QueueUpdatePacket queueUpdatePacket = new QueueUpdatePacket(timestamp, videoQueue);
			
			//Send initial queue to client
			dout.write((queueUpdatePacket).toByteArray());
			//Now we deal with the client on the seperate thread
			
		}
	}

}
