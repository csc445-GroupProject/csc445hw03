package edu.oswego.cs.ytsync.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet {
    private byte[] payload;
    private Opcode op;
    private long timestamp;
    private int size;

    public Packet(Opcode op, long timestamp) {
        this.op = op;
        this.timestamp = timestamp;
        size = 0;
        payload = null;
    }

    public Packet(Opcode op, long timestamp, byte[] payload) {
        this(op, timestamp);
        setPayload(payload);
    }

    public static Packet fromByteArray(byte[] a) {
        ByteBuffer buffer = ByteBuffer.wrap(a);

        byte opOrd = buffer.get();
        Opcode op = Opcode.values()[(int) opOrd];

        long timestamp = buffer.getLong();
        int size = buffer.getInt();

        Packet packet = new Packet(op, timestamp);

        if (size > 0) {
            byte[] payload = new byte[size];
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
        this.size = payload.length;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(1400);

        buffer.put((byte) op.ordinal());
        buffer.putLong(timestamp);

        if (payload != null) {
            buffer.putInt(size);
            buffer.put(payload);
        }

        return buffer.array();
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