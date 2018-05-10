package edu.oswego.cs.ytsync.common;

import java.io.*;

public class LeaderVotePacket extends Packet {
    public LeaderVotePacket(Long timestamp, String hostname, int electionTerm){
        super(Opcode.VOTE, timestamp);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream payloadOutputStream = new DataOutputStream(byteOutputStream);

        try {
            payloadOutputStream.writeInt(electionTerm);
            payloadOutputStream.writeUTF(hostname);


            setPayload(byteOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LeaderVotePacket(Packet p) {super(p.getOp(), p.getTimestamp(), p.getPayload());}

    public int getElectionTerm() throws IOException{
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(getPayload());
        DataInputStream termInputStream = new DataInputStream(byteInputStream);
        return termInputStream.readInt();
    }

    public String getHostname() throws IOException{
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(getPayload());
        DataInputStream hostnameInputStream = new DataInputStream(byteInputStream);
        int term = hostnameInputStream.readInt();
        return hostnameInputStream.readUTF();
    }
}
