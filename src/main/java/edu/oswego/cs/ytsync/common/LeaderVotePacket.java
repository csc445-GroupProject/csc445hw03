package edu.oswego.cs.ytsync.common;

public class LeaderVotePacket extends Packet {
    public LeaderVotePacket(Long timestamp, String hostname){
        super(Opcode.VOTE, timestamp);
        this.setPayload(hostname.getBytes());
    }

    public LeaderVotePacket(Packet p) {super(p.getOp(), p.getTimestamp(), p.getPayload());}

    public String getHostname() {return new String(getPayload());}
}
