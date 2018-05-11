package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemoveQueuePacketTest {

    @Test
    public void getId() {
        RemoveQueuePacket packet = new RemoveQueuePacket(System.currentTimeMillis(), "dQw4w9WgXcQ");
        assertEquals("dQw4w9WgXcQ", packet.getId());
    }
}