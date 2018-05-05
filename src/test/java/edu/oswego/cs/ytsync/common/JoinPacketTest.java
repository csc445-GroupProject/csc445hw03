package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class JoinPacketTest {

    @Test
    public void getUsername() {
        JoinPacket packet = new JoinPacket(System.currentTimeMillis(), "xXlimp_bizkitXx");
        assertEquals("xXlimp_bizkitXx", packet.getUsername());
    }
}