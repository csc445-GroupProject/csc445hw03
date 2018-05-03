package packetpackage;

import java.nio.ByteBuffer;

public class ServerAck {
	
	//public enum Opcode { ACK; }
	
	//private Opcode op;
	private int roomNumbers;
	private long timestamp;
	private int size;
	private byte[] payload;
	
	
	public ServerAck(int roomNumbers, long timestamp){
		//this.op = op;
		this.roomNumbers = roomNumbers;
		this.timestamp = timestamp;
	}
	
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(1400);
		
		buffer.putInt(roomNumbers);
		buffer.putLong(timestamp);
		
		return buffer.array();
	}
}
