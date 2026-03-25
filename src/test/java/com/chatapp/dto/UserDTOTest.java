package com.chatapp.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void constructor_AllArgs_ShouldSetAllFields() {
        // When
        UserDTO dto = new UserDTO(1L, "testuser", "ONLINE");

        // Then
        assertEquals(1L, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("ONLINE", dto.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        UserDTO dto = new UserDTO(1L, "user1", "ONLINE");

        // When
        dto.setId(2L);
        dto.setUsername("user2");
        dto.setStatus("OFFLINE");

        // Then
        assertEquals(2L, dto.getId());
        assertEquals("user2", dto.getUsername());
        assertEquals("OFFLINE", dto.getStatus());
    }

    @Test
    void constructor_NoArgs_ShouldCreateEmptyDTO() {
        // When
        UserDTO dto = new UserDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getUsername());
        assertNull(dto.getStatus());
    }
}
