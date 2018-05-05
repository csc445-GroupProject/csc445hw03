package edu.oswego.cs.ytsync.common;

public class JoinPacket extends Packet{
    public JoinPacket(long timestamp, String username) {
        super(Opcode.JOIN, timestamp);
        this.setPayload(username.getBytes());
    }

    public String getUsername() {
        return new String(getPayload());
    }
}
