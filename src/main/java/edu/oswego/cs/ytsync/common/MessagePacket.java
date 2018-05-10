package edu.oswego.cs.ytsync.common;

public class MessagePacket extends Packet {
    public MessagePacket(Long timestamp, String message, String username) {
        super(Opcode.CHAT, timestamp);
        String fullMessage = String.format("%s %d: %s", username, timestamp, message);
        this.setPayload(fullMessage.getBytes());
    }

    public MessagePacket(Packet p) { super(p.getOp(), p.getTimestamp(), p.getPayload()); }

    public String getMessage() { return new String(getPayload()); }
}
