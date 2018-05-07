package edu.oswego.cs.ytsync.common;

public class MessagePacket extends Packet {
    public MessagePacket(Long timestamp, String message) {
        super(Opcode.CHAT, timestamp);
        this.setPayload(message.getBytes());
    }

    public MessagePacket(Packet p) { super(p.getOp(), p.getTimestamp(), p.getPayload()); }

    public String getMessage() { return new String(getPayload()); }
}
