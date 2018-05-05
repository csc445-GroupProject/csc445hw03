package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectPacketTest {

    @Test
    public void getUsername() {
        ConnectPacket packet = new ConnectPacket(System.currentTimeMillis(), "xXlimp_bizkitXx");
        assertEquals("xXlimp_bizkitXx", packet.getUsername());
    }
}