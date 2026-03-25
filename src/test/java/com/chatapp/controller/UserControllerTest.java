package com.chatapp.controller;

import com.chatapp.dto.UserDTO;
import com.chatapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO(1L, "testuser", "ONLINE");
    }

    @Test
    void getOnlineUsers_ShouldReturnUserList() {
        // Given
        UserDTO user1 = new UserDTO(1L, "user1", "ONLINE");
        UserDTO user2 = new UserDTO(2L, "user2", "ONLINE");
        List<UserDTO> onlineUsers = Arrays.asList(user1, user2);
        when(userService.getOnlineUsers()).thenReturn(onlineUsers);

        // When
        List<UserDTO> result = userController.getOnlineUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userService).getOnlineUsers();
    }

    @Test
    void getOnlineUsers_NoUsers_ShouldReturnEmptyList() {
        // Given
        when(userService.getOnlineUsers()).thenReturn(Arrays.asList());

        // When
        List<UserDTO> result = userController.getOnlineUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService).getOnlineUsers();
    }

    @Test
    void connect_ValidUsername_ShouldReturnOkWithUserDTO() {
        // Given
        when(userService.connectUser("newuser")).thenReturn(testUserDTO);

        // When
        ResponseEntity<UserDTO> response = userController.connect("newuser");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("ONLINE", response.getBody().getStatus());
        verify(userService).connectUser("newuser");
    }

    @Test
    void connect_DifferentUsername_ShouldCallServiceWithCorrectParam() {
        // Given
        UserDTO anotherUser = new UserDTO(2L, "anotheruser", "ONLINE");
        when(userService.connectUser("anotheruser")).thenReturn(anotherUser);

        // When
        ResponseEntity<UserDTO> response = userController.connect("anotheruser");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("anotheruser", response.getBody().getUsername());
        verify(userService).connectUser("anotheruser");
    }

    @Test
    void disconnect_ValidUsername_ShouldReturnOk() {
        // Given
        doNothing().when(userService).disconnectUser("testuser");

        // When
        ResponseEntity<Void> response = userController.disconnect("testuser");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).disconnectUser("testuser");
    }

    @Test
    void disconnect_DifferentUsername_ShouldCallServiceWithCorrectParam() {
        // Given
        doNothing().when(userService).disconnectUser("anotheruser");

        // When
        ResponseEntity<Void> response = userController.disconnect("anotheruser");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).disconnectUser("anotheruser");
    }
}
