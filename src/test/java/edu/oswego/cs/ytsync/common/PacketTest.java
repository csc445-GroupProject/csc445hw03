package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PacketTest {
    @Test
    public void testByteArray() {
        Packet packet = new Packet(Opcode.CONNECT, 420);
        packet.setPayload("hello".getBytes());
        byte[] bytes = packet.toByteArray();
        Packet other = Packet.fromByteArray(bytes);

        assertEquals(packet, other);
    }
}