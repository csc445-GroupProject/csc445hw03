import java.nio.ByteBuffer;

public class SyncPacket {
    public enum Opcode {
        ACK,
        JOIN,
        LEAVE,
        ADD_QUEUE,
        REMOVE_QUEUE;
    }

    private Opcode op;
    private int seqNo;
    private long timestamp;
    private int size;
    byte[] payload;

    public SyncPacket(Opcode op, int seqNo, long timestamp, int size) {
        this.op = op;
        this.seqNo = seqNo;
        this.timestamp = timestamp;
        size = 0;
        payload = null;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        this.size = payload.length;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(1400);

        buffer.put((byte) op.ordinal());
        buffer.putInt(seqNo);
        buffer.putLong(timestamp);

        if(payload != null) {
            buffer.putInt(size);
            buffer.put(payload);
        }

        return buffer.array();
    }

    public static SyncPacket fromByteArray(byte[] a) {
        ByteBuffer buffer = ByteBuffer.wrap(a);

        byte opOrd = buffer.get();
        Opcode op = Opcode.values()[(int) opOrd];

        int seqNo = buffer.getInt();
        long timestamp = buffer.getLong();
        int size = buffer.getInt();

        SyncPacket packet = new SyncPacket(op, seqNo, timestamp, size);

        if(size > 0) {
            byte[] payload = new byte[size];
            buffer.get(payload);
            packet.setPayload(payload);
        }

        return packet;
    }
}