package edu.oswego.cs.ytsync.server;

import edu.oswego.cs.ytsync.Client;
import edu.oswego.cs.ytsync.common.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PseudoColumnUsage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ClientHandler implements Runnable {
	final DataOutputStream dout;
	final DataInputStream din;
	final Socket socket;
	//final Client client;
	final PacketStream ps;
	final ConcurrentLinkedQueue<String> clq;
	final PriorityBlockingQueue<String> pbq;

	public ClientHandler(Socket socket, ConcurrentLinkedQueue<String> clq, PriorityBlockingQueue<String> pbq) throws IOException{
		//this.client = client;
		this.socket = socket;
		this.din = new DataInputStream(socket.getInputStream());
		this.dout = new DataOutputStream(socket.getOutputStream());
		this.ps = new PacketStream(din);
		this.clq = clq;
		this.pbq = pbq;
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

		while (true) {

			while(!ps.hasNext()) {
                ps.bufferPackets();
            } 
            Packet request = ps.next();
			Opcode operation = request.getOp();
			
			switch(operation) {
				case CONNECT: {
					if(request.getPayload() == null) {
						request.setPayload(Thread.currentThread().getName().getBytes());
					}
					ConnectPacket rq = new ConnectPacket(request);
					//ConnectPacket response = new ConnectPacket(System.currentTimeMillis(),Thread.currentThread().getName());
                    System.out.println(operation + " Connected: " + rq.getUsername());
                    
                    break;
                }
				case ADD_QUEUE: {
                    System.out.println(operation);
					AddQueuePacket addQueuePacket = new AddQueuePacket(request);
					String url = addQueuePacket.getId();
					pbq.add(url);
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
					MessagePacket messagePacket = new MessagePacket(request);
					String message = messagePacket.getMessage();
					System.out.println(operation + " new message: " + message);
					clq.add(message);
					System.out.println(message);
					while(clq.size() > 1000) {
						System.out.println("removed: " + clq.remove());
					}
					break;
				}
			}
		}
	}
}
