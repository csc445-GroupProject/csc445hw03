package serverpackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import packetpackage.*;

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
			firstACK.setPayload(tempBuf.array(););
			
			//Send packet ACK packet to client
			dout.write((firstACK).toByteArray());
			//Now we deal with the client on the seperate thread
			
		}
	}

}

class ClientHandler extends Thread {
	final DataOutputStream dout;
	final DataInputStream din;
	final Socket socket;

	public ClientHandler(Socket socket, DataInputStream din, DataOutputStream dout) {
		this.socket = socket;
		this.din = din;
		this.dout = dout;
	}

	public SyncPacket readPacket(byte[] a) {
		SyncPacket clientPacket = SyncPacket.fromByteArray(a);
		return clientPacket;
	}

	@Override
	public void run() {
		//Wait for client response
		while (true) {
			byte[] packetBuffer = new byte[1400];
			try {
				din.read(packetBuffer);
			} catch(IOException e) {
				System.out.println("Cant read buffer");
			}
			//recieve, decode, and read packet from client
			SyncPacket incomingPacket = SyncPacket.fromByteArray(packetBuffer);
			SyncPacket.Opcode operation = incomingPacket.getOp();
			switch(operation) {
				case JOIN:
					
				case LEAVE:
				case ADD_QUEUE:
				case REMOVE_QUEUE:
			}
		}
	}
}

class ClientRoomHandler extends Thread {
	final DataOutputStream dout;
	final DataInputStream din;
	final ServerSocket roomServer;
	final int roomNum;
	final int serverPort;
	final Socket socket;
	
	public ClientRoomHandler(int roomNumber) throws IOException {
		this.roomServer = new ServerSocket(0);
		this.serverPort = roomServer.getLocalPort();
		this.roomNum = roomNumber;
		this.socket = roomServer.accept();
		this.dout = new DataOutputStream(socket.getOutputStream());
		this.din = new DataInputStream(socket.getInputStream());
	}
}
