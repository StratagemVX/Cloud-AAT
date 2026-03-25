package com.chatapp.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageDTOTest {

    @Test
    void constructor_AllArgs_ShouldSetAllFields() {
        // When
        MessageDTO dto = new MessageDTO("sender1", "Hello", "CHAT", "14:30");

        // Then
        assertEquals("sender1", dto.getSender());
        assertEquals("Hello", dto.getContent());
        assertEquals("CHAT", dto.getType());
        assertEquals("14:30", dto.getTimestamp());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        MessageDTO dto = new MessageDTO("sender1", "Hello", "CHAT", "14:30");

        // When
        dto.setSender("sender2");
        dto.setContent("Goodbye");
        dto.setType("LEAVE");
        dto.setTimestamp("15:00");

        // Then
        assertEquals("sender2", dto.getSender());
        assertEquals("Goodbye", dto.getContent());
        assertEquals("LEAVE", dto.getType());
        assertEquals("15:00", dto.getTimestamp());
    }

    @Test
    void constructor_NoArgs_ShouldCreateEmptyDTO() {
        // When
        MessageDTO dto = new MessageDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getSender());
        assertNull(dto.getContent());
        assertNull(dto.getType());
        assertNull(dto.getTimestamp());
    }

    @Test
    void constructor_JoinMessage_ShouldCreateCorrectly() {
        // When
        MessageDTO dto = new MessageDTO("user1", "user1 joined", "JOIN", null);

        // Then
        assertEquals("user1", dto.getSender());
        assertEquals("user1 joined", dto.getContent());
        assertEquals("JOIN", dto.getType());
        assertNull(dto.getTimestamp());
    }
}
