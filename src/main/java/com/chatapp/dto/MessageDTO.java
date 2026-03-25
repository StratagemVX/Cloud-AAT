package com.chatapp.dto;

/**
 * Data-transfer object for chat messages sent over WebSocket.
 *
 * <p>
 * Used in both directions: the client sends a MessageDTO when
 * publishing a message, and the server broadcasts one back to
 * all subscribers.
 * </p>
 */
public class MessageDTO {

    private String sender;
    private String content;
    private String type; // CHAT | JOIN | LEAVE | BOT
    private String timestamp;

    // ─── Constructors ──────────────────────────────────────────────────
    public MessageDTO() {
    }

    public MessageDTO(String sender, String content, String type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    public MessageDTO(String sender, String content, String type, String timestamp) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
