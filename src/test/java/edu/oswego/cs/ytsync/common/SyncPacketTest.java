package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class SyncPacketTest {
    @Test
    public void testByteArray() {
        SyncPacket packet = new SyncPacket(SyncPacket.Opcode.JOIN, 0, 420);
        packet.setPayload("hello".getBytes());
        byte[] bytes = packet.toByteArray();
        SyncPacket other = SyncPacket.fromByteArray(bytes);

        assertEquals(packet, other);
    }
}