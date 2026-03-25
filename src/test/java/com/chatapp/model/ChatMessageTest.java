package com.chatapp.model;

import com.chatapp.model.ChatMessage.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    @Test
    void constructor_AllArgs_ShouldSetFields() {
        // When
        ChatMessage message = new ChatMessage("sender1", "Hello World", MessageType.CHAT);

        // Then
        assertEquals("sender1", message.getSender());
        assertEquals("Hello World", message.getContent());
        assertEquals(MessageType.CHAT, message.getType());
    }

    @Test
    void constructor_NoArgs_ShouldCreateEmptyMessage() {
        // When
        ChatMessage message = new ChatMessage();

        // Then
        assertNotNull(message);
        assertNull(message.getSender());
        assertNull(message.getContent());
        assertEquals(MessageType.CHAT, message.getType()); // Default value
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        ChatMessage message = new ChatMessage();

        // When
        message.setId(1L);
        message.setSender("user1");
        message.setContent("Test content");
        message.setType(MessageType.JOIN);

        // Then
        assertEquals(1L, message.getId());
        assertEquals("user1", message.getSender());
        assertEquals("Test content", message.getContent());
        assertEquals(MessageType.JOIN, message.getType());
    }

    @Test
    void messageType_Enum_ShouldHaveCorrectValues() {
        // Then
        assertEquals(4, MessageType.values().length);
        assertEquals("CHAT", MessageType.CHAT.name());
        assertEquals("JOIN", MessageType.JOIN.name());
        assertEquals("LEAVE", MessageType.LEAVE.name());
        assertEquals("BOT", MessageType.BOT.name());
    }

    @Test
    void constructor_JoinType_ShouldCreateCorrectly() {
        // When
        ChatMessage message = new ChatMessage("user1", "user1 joined", MessageType.JOIN);

        // Then
        assertEquals("user1", message.getSender());
        assertEquals("user1 joined", message.getContent());
        assertEquals(MessageType.JOIN, message.getType());
    }

    @Test
    void constructor_LeaveType_ShouldCreateCorrectly() {
        // When
        ChatMessage message = new ChatMessage("user1", "user1 left", MessageType.LEAVE);

        // Then
        assertEquals("user1", message.getSender());
        assertEquals("user1 left", message.getContent());
        assertEquals(MessageType.LEAVE, message.getType());
    }
}
