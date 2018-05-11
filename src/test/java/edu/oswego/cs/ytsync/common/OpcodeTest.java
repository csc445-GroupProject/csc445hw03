package edu.oswego.cs.ytsync.common;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OpcodeTest {

    @Test
    public void opcodeCount() {
        assertTrue(Opcode.values().length <= Byte.MAX_VALUE);
    }
}