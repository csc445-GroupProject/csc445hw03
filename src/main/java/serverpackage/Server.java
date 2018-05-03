package serverpackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import packetpackage.*;

public class Server {

	private final static int port = 2706;

	public static void main(String args[]) throws IOException {

		ServerSocket host = new ServerSocket(port);
		int clients = 0;
		int rooms = 0;
		long timestamp;
		while (true) {
			Socket client = host.accept();
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());
			new Thread(() -> {
				ClientHandler conClient = new ClientHandler(client, dis, dout);
			}, "Client: " + clients).start();
			timestamp = System.nanoTime();
			dout.write(new ServerAck(rooms,timestamp).toByteArray());
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

	public void CreateNewRoom() {

	}

	public SyncPacket readPacket(byte[] a) {
		SyncPacket clientPacket = SyncPacket.fromByteArray(a);
		return clientPacket;
	}

	@Override
	public void run() {
		while (true) {
			// Handle what client threads do
			// Take requests: add queue, remove queue, chat, leave and etc.
			byte[] packetBuffer = new byte[1400];
			try {
				din.read(packetBuffer);
			} catch(IOException e) {
				System.out.println("Cant read buffer");
			}
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
	final Socket socket;
	
	public ClientRoomHandler(int roomNumber, Socket socket, DataOutputStream dout, DataInputStream din) {
		this.socket = socket;
		this.dout = dout;
		this.din = din;
	}
}
