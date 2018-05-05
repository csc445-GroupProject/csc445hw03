package edu.oswego.cs.ytsync.common;

public class AddQueuePacket extends Packet {
    public AddQueuePacket(long timestamp, String id) {
        super(Opcode.ADD_QUEUE, timestamp);
        this.setPayload(id.getBytes());
    }

    public String getId() {
        return new String(getPayload());
    }
}
