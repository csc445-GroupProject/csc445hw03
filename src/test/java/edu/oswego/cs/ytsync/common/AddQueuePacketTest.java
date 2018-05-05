package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class AddQueuePacketTest {

    @Test
    public void getId() {
        AddQueuePacket packet = new AddQueuePacket(System.currentTimeMillis(), "dQw4w9WgXcQ");
        assertEquals("dQw4w9WgXcQ", packet.getId());
    }
}