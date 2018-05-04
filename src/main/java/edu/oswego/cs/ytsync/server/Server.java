package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import edu.oswego.cs.ytsync.common.*;

public class Server {

	private final static int port = 2706;

	public static void main(String args[]) throws IOException {

		ServerSocket host = new ServerSocket(port);
		int clients = 0;
		int rooms = 0;
		long timestamp;
		while (true) {
			//Client first connects to the server
			Socket client = host.accept(); 
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
			
			//send Ack packet with room info and timestamp
			//Currently only adding room number to payload **and not sure wat do with seqNum
			SyncPacket firstACK = new SyncPacket(SyncPacket.Opcode.ACK, 0, timestamp);
			ByteBuffer tempBuf = ByteBuffer.allocate(1400);
			tempBuf.put((byte)rooms);
			firstACK.setPayload(tempBuf.array());
			
			//Send packet ACK packet to client
			dout.write((firstACK).toByteArray());
			//Now we deal with the client on the seperate thread
			
		}
	}

}
