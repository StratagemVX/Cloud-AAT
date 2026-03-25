package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single message in the chat.
 *
 * <p>
 * The {@code type} field distinguishes normal chat messages from
 * system events (JOIN/LEAVE) and chatbot responses (BOT).
 * </p>
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the sender (or "ChatBot" for bot replies). */
    @Column(nullable = false, length = 50)
    private String sender;

    /** The textual content of the message. */
    @Column(nullable = false, length = 2000)
    private String content;

    /** Message type discriminator. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.CHAT;

    /** Server-side timestamp when the message was persisted. */
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // ─── Enum ──────────────────────────────────────────────────────────
    public enum MessageType {
        CHAT, // Normal user message
        JOIN, // User joined notification
        LEAVE, // User left notification
        BOT // Chatbot response
    }

    // ─── Lifecycle callback ────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // ─── Constructors ──────────────────────────────────────────────────
    public ChatMessage() {
    }

    public ChatMessage(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
