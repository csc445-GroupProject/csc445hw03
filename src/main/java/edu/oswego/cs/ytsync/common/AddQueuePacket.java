package edu.oswego.cs.ytsync.common;

public class AddQueuePacket extends Packet {
    public AddQueuePacket(long timestamp, String id) {
        super(Opcode.ADD_QUEUE, timestamp);
        this.setPayload(id.getBytes());
    }

    public AddQueuePacket(Packet p) {
        super(p.getOp(), p.getTimestamp(), p.getPayload());
    }

    public String getId() {
        return new String(getPayload());
    }
}
