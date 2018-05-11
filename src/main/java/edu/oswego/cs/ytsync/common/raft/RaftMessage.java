package edu.oswego.cs.ytsync.common.raft;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RaftMessage {
    private MessageType type;
    private Integer term;
    private Integer leaderId;
    private Integer prevLogIndex;
    private Integer prevLogTerm;
    private List<LogEntry> entries;
    private LogEntry entry;
    private Integer leaderCommit;
    private Integer candidateId;
    private Integer lastLogIndex;
    private Integer lastLogTerm;
    private Boolean success;
    private Boolean voteGranted;

    private RaftMessage(MessageType type, Integer term, Integer leaderId, Integer prevLogIndex, Integer prevLogTerm,
                        List<LogEntry> entries, LogEntry entry, Integer leaderCommit, Integer candidateId,
                        Integer lastLogIndex, Integer lastLogTerm, Boolean success, Boolean voteGranted) {
        this.type = type;
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.entry = entry;
        this.leaderCommit = leaderCommit;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
        this.success = success;
        this.voteGranted = voteGranted;
    }

    public static RaftMessage voteRequest(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        return new RaftMessage(MessageType.VOTE_REQUEST, term, null, null, null, null, null, null, candidateId, lastLogIndex,
                lastLogTerm, null, null);
    }

    public static RaftMessage voteResponse(int term, boolean voteGranted) {
        return new RaftMessage(MessageType.VOTE_RESOPNSE, term, null, null, null, null, null, null, null, null, null, null, voteGranted);
    }

    public static RaftMessage appendRequest(int term, int leaderId, int prevLogIndex, int prevLogTerm, List<LogEntry> entries, int leaderCommit) {
        return new RaftMessage(MessageType.APPEND_REQUEST, term, leaderId, prevLogIndex, prevLogTerm, entries, null, leaderCommit, null, null, null, null, null);
    }

    public static RaftMessage appendResponse(int term, boolean success) {
        return new RaftMessage(MessageType.APPEND_RESPONSE, term, null, null, null, null, null, null, null, null, null, success, null);
    }

    public static RaftMessage chatMessage(LogEntry entry) {
        return new RaftMessage(MessageType.CHAT_MESSAGE, null, null, null, null, null, entry, null, null, null, null, null, null);
    }

    ;

    public static RaftMessage fromByteArray(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(bais);

        MessageType type;
        try {
            type = MessageType.values()[in.readInt()];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(e);
        }

        switch (type) {
            case VOTE_REQUEST: {
                int term = in.readInt();
                int candidateId = in.readInt();
                int lastLogIndex = in.readInt();
                int lastLogTerm = in.readInt();
                return RaftMessage.voteRequest(term, candidateId, lastLogIndex, lastLogTerm);
            }
            case VOTE_RESOPNSE: {
                int term = in.readInt();
                boolean voteGranted = in.readBoolean();
                return RaftMessage.voteResponse(term, voteGranted);
            }
            case APPEND_REQUEST: {
                int term = in.readInt();
                int leaderId = in.readInt();
                int prevLogIndex = in.readInt();
                int prevLogTerm = in.readInt();
                int size = in.readInt();
                List<LogEntry> entries = new ArrayList<>();

                for (int i = 0; i < size; i++) {
                    int entryTerm = in.readInt();
                    String username = in.readUTF();
                    String chatMessage = in.readUTF();
                    entries.add(new LogEntry(entryTerm, username, chatMessage));
                }

                int leaderCommit = in.readInt();
                return RaftMessage.appendRequest(term, leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit);
            }
            case APPEND_RESPONSE: {
                int term = in.readInt();
                boolean success = in.readBoolean();
                return RaftMessage.appendResponse(term, success);
            }
            case CHAT_MESSAGE: {
                int term = in.readInt();
                String username = in.readUTF();
                String chatMessage = in.readUTF();
                return RaftMessage.chatMessage(new LogEntry(term, username, chatMessage));
            }
        }

        return null;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            switch (type) {
                case VOTE_REQUEST: {
                    out.writeInt(type.ordinal());
                    out.writeInt(term);
                    out.writeInt(candidateId);
                    out.writeInt(lastLogIndex);
                    out.writeInt(lastLogTerm);
                    break;
                }
                case VOTE_RESOPNSE: {
                    out.writeInt(type.ordinal());
                    out.writeInt(term);
                    out.writeBoolean(voteGranted);
                    break;
                }
                case APPEND_REQUEST: {
                    out.writeInt(type.ordinal());
                    out.writeInt(term);
                    out.writeInt(leaderId);
                    out.writeInt(prevLogIndex);
                    out.writeInt(prevLogTerm);
                    out.writeInt(entries.size());

                    for (LogEntry e : entries) {
                        out.writeInt(e.getTerm());
                        out.writeUTF(e.getUsername());
                        out.writeUTF(e.getChatMessage());
                    }

                    out.writeInt(leaderCommit);
                    break;
                }
                case APPEND_RESPONSE: {
                    out.writeInt(type.ordinal());
                    out.writeInt(term);
                    out.writeBoolean(success);
                    break;
                }
                case CHAT_MESSAGE: {
                    out.writeInt(type.ordinal());
                    out.writeInt(entry.getTerm());
                    out.writeUTF(entry.getUsername());
                    out.writeUTF(entry.getChatMessage());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaftMessage that = (RaftMessage) o;
        return type == that.type &&
                Objects.equals(term, that.term) &&
                Objects.equals(leaderId, that.leaderId) &&
                Objects.equals(prevLogIndex, that.prevLogIndex) &&
                Objects.equals(prevLogTerm, that.prevLogTerm) &&
                Objects.equals(entries, that.entries) &&
                Objects.equals(entry, that.entry) &&
                Objects.equals(leaderCommit, that.leaderCommit) &&
                Objects.equals(candidateId, that.candidateId) &&
                Objects.equals(lastLogIndex, that.lastLogIndex) &&
                Objects.equals(lastLogTerm, that.lastLogTerm) &&
                Objects.equals(success, that.success) &&
                Objects.equals(voteGranted, that.voteGranted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, term, leaderId, prevLogIndex, prevLogTerm, entries, entry, leaderCommit, candidateId, lastLogIndex, lastLogTerm, success, voteGranted);
    }
}
