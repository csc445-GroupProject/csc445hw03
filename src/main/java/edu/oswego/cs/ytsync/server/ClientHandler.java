package edu.oswego.cs.ytsync.server;

import edu.oswego.cs.ytsync.common.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler extends Thread {
	final DataOutputStream dout;
	final DataInputStream din;
	final Socket socket;

	public ClientHandler(Socket socket, DataInputStream din, DataOutputStream dout) {
		this.socket = socket;
		this.din = din;
		this.dout = dout;
	}

	public Packet readPacket(byte[] a) {
		Packet clientPacket = Packet.fromByteArray(a);
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
			Packet incomingPacket = Packet.fromByteArray(packetBuffer);
			Opcode operation = incomingPacket.getOp();
			switch(operation) {
				case JOIN: {
                    JoinPacket joinPacket = (JoinPacket) incomingPacket;
                    break;
                }
				case ADD_QUEUE: {
                    AddQueuePacket addQueuePacket = (AddQueuePacket) incomingPacket;
                    break;
                }
				case REMOVE_QUEUE: {
                    RemoveQueuePacket removeQueuePacket = (RemoveQueuePacket) incomingPacket;
                }
			}
		}
	}
}
