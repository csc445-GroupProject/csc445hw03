package edu.oswego.cs.ytsync.common;

public class RemoveQueuePacket extends Packet {
    public RemoveQueuePacket(long timestamp, String id) {
        super(Opcode.REMOVE_QUEUE, timestamp);
        this.setPayload(id.getBytes());
    }

    public RemoveQueuePacket(Packet p) {
        super(p.getOp(), p.getTimestamp(), p.getPayload());
    }

    public String getId() {
        return new String(getPayload());
    }
}