package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class QueueUpdatePacketTest {

    @Test
    public void getIds() throws IOException {
        List<String> ids = new ArrayList<>();

        ids.add("dQw4w9WgXcQ");
        ids.add("djV11Xbc914");
        ids.add("FTQbiNvZqaY");
        ids.add("dv13gl0a-FA");

        QueueUpdatePacket packet = new QueueUpdatePacket(System.currentTimeMillis(), ids);

        assertEquals(ids, packet.getIds());
    }
}