package edu.oswego.cs.ytsync.common.raft;

import java.util.Objects;

public class LogEntry {
    private int term;
    private String username;
    private String chatMessage;

    public LogEntry(int term, String username, String chatMessage) {
        this.term = term;
        this.username = username;
        this.chatMessage = chatMessage;
    }

    public int getTerm() {
        return term;
    }

    public String getUsername() {
        return username;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return term == logEntry.term &&
                Objects.equals(username, logEntry.username) &&
                Objects.equals(chatMessage, logEntry.chatMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, username, chatMessage);
    }
}
