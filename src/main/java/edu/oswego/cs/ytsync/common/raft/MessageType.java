package edu.oswego.cs.ytsync.common.raft;

public enum MessageType {
    VOTE_REQUEST, VOTE_RESPONSE, APPEND_REQUEST, APPEND_RESPONSE, CHAT_MESSAGE
}
