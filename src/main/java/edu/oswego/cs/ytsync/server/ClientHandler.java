package edu.oswego.cs.ytsync.server;

import edu.oswego.cs.ytsync.client.Client;
import edu.oswego.cs.ytsync.common.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PseudoColumnUsage;

public class ClientHandler implements Runnable {
	final DataOutputStream dout;
	final DataInputStream din;
	final Socket socket;
	//final Client client;
	final PacketStream ps;

	public ClientHandler(Socket socket) throws IOException{
		//this.client = client;
		this.socket = socket;
		this.din = new DataInputStream(socket.getInputStream());
		this.dout = new DataOutputStream(socket.getOutputStream());
		this.ps = new PacketStream(din);
	}
	
	public DataInputStream getDataInputStream() {
		return this.din;
	}
	
	public DataOutputStream getDataOutputStream() {
		return this.dout;
	}

	public Packet readPacket(byte[] a) {
		Packet clientPacket = Packet.fromByteArray(a);
		return clientPacket;
	}

	@Override
	public void run() {
		//client connects, exchange ConnectPacket
		//First recieve

		while (true) {
			System.out.println("ayaya");
			byte[] packetBuffer = new byte[2400];
			try {
				din.read(packetBuffer);
			} catch(IOException e) {
				System.out.println("Cant read buffer: total" + (10000 - packetBuffer.length));
			}
			//recieve, decode, and read packet from client
			Packet request = Packet.fromByteArray(packetBuffer);
			Opcode operation = request.getOp();
			System.out.println(operation);
			switch(operation) {
				case CONNECT: {
					ConnectPacket rq = new ConnectPacket(request);
					//ConnectPacket response = new ConnectPacket(System.currentTimeMillis(),Thread.currentThread().getName());
                    System.out.println(operation + " Connected: " + rq.getUsername());
                    //need user info added to payload
                    //Add username to list of clients
                    break;
                }
				case ADD_QUEUE: {
                    System.out.println(operation);
					AddQueuePacket addQueuePacket = new AddQueuePacket(request);
					String url = addQueuePacket.getId();
                    break;
                }
				case REMOVE_QUEUE: {
                    System.out.println(operation);
					RemoveQueuePacket removeQueuePacket = new RemoveQueuePacket(request);
                }
				case QUEUE_UPDATE: {
					
					break;
				}
				case CHAT: {
					
					break;
				}
			}
		}
	}
}
