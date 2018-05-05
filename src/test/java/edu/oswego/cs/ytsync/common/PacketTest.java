package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class PacketTest {
    @Test
    public void testByteArray() {
        Packet packet = new Packet(Opcode.JOIN, 420);
        packet.setPayload("hello".getBytes());
        byte[] bytes = packet.toByteArray();
        Packet other = Packet.fromByteArray(bytes);

        assertEquals(packet, other);
    }
}