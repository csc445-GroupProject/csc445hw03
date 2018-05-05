package edu.oswego.cs.ytsync.common;

public class ConnectPacket extends Packet{
    public ConnectPacket(long timestamp, String username) {
        super(Opcode.CONNECT, timestamp);
        this.setPayload(username.getBytes());
    }

    public String getUsername() {
        return new String(getPayload());
    }
}
