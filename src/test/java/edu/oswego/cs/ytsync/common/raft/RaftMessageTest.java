package edu.oswego.cs.ytsync.common.raft;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RaftMessageTest {

    @Test
    public void voteRequest() throws IOException {
        RaftMessage expected = RaftMessage.voteRequest(4, 6, 8, 3);
        RaftMessage result = RaftMessage.fromByteArray(expected.toByteArray());
        assertEquals(expected, result);
    }

    @Test
    public void voteResponse() throws IOException {
        RaftMessage expected = RaftMessage.voteResponse(5, true);
        RaftMessage result = RaftMessage.fromByteArray(expected.toByteArray());
        assertEquals(expected, result);
    }

    @Test
    public void appendRequest() throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        entries.add(new LogEntry(3, "user1", "message1"));
        entries.add(new LogEntry(5, "user2", "message2"));
        entries.add(new LogEntry(8, "user3", "message3"));
        entries.add(new LogEntry(6, "user4", "message4"));
        RaftMessage expected = RaftMessage.appendRequest(4, 8, 7, 3, entries, 6);
        RaftMessage result = RaftMessage.fromByteArray(expected.toByteArray());
        assertEquals(expected, result);
    }

    @Test
    public void appendResponse() throws IOException {
        RaftMessage expected = RaftMessage.appendResponse(6, false);
        RaftMessage result = RaftMessage.fromByteArray(expected.toByteArray());
        assertEquals(expected, result);
    }

    @Test
    public void chatMessage() throws IOException {
        RaftMessage expected = RaftMessage.chatMessage(new LogEntry(3, "user1", "message1"));
        RaftMessage result = RaftMessage.fromByteArray(expected.toByteArray());
        assertEquals(expected, result);
    }
}