package edu.oswego.cs.ytsync.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    private final static int HEADER_SIZE = 13;

    private byte[] payload;
    private Opcode op;
    private long timestamp;
    private int size;

    public Packet(Opcode op, long timestamp) {
        this.op = op;
        this.timestamp = timestamp;
        size = HEADER_SIZE;
        payload = null;
    }

    public Packet(Opcode op, long timestamp, byte[] payload) {
        this(op, timestamp);
        setPayload(payload);
    }

    public static Packet fromByteArray(byte[] a) {
        ByteBuffer buffer = ByteBuffer.wrap(a);;

        int size = buffer.getInt();
        byte opOrd = buffer.get();
        Opcode op = Opcode.values()[(int) opOrd];
        long timestamp = buffer.getLong();

        Packet packet = new Packet(op, timestamp);

        if (size > HEADER_SIZE) {
            byte[] payload = new byte[size - HEADER_SIZE];
            buffer.get(payload);
            packet.setPayload(payload);
        }

        return packet;
    }

    public Opcode getOp() {
        return op;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getSize() {
        return size;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        this.size = HEADER_SIZE + payload.length;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(byteStream);

        try {
            outStream.writeInt(size);
            outStream.writeByte((byte) op.ordinal());
            outStream.writeLong(timestamp);

            if (payload != null) {
                outStream.write(payload);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteStream.toByteArray();
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Packet))
            return false;

        Packet other = (Packet) o;
        return this.op == other.getOp() && this.timestamp == other.getTimestamp()
                && this.size == other.getSize() && Arrays.equals(this.payload, other.getPayload());
    }
}