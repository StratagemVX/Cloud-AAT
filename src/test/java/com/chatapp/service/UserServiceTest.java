package com.chatapp.service;

import com.chatapp.dto.UserDTO;
import com.chatapp.model.User;
import com.chatapp.model.User.UserStatus;
import com.chatapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser");
        testUser.setId(1L);
        testUser.setStatus(UserStatus.ONLINE);
    }

    @Test
    void connectUser_NewUser_ShouldCreateAndReturnUserDTO() {
        // Given
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // When
        UserDTO result = userService.connectUser("newuser");

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("ONLINE", result.getStatus());
        verify(userRepository).findByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void connectUser_ExistingUser_ShouldSetOnlineAndReturn() {
        // Given
        testUser.setStatus(UserStatus.OFFLINE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        UserDTO result = userService.connectUser("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("ONLINE", result.getStatus());
        assertEquals(UserStatus.ONLINE, testUser.getStatus());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(testUser);
    }

    @Test
    void disconnectUser_ExistingUser_ShouldSetOffline() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        userService.disconnectUser("testuser");

        // Then
        assertEquals(UserStatus.OFFLINE, testUser.getStatus());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(testUser);
    }

    @Test
    void disconnectUser_NonExistentUser_ShouldDoNothing() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        userService.disconnectUser("nonexistent");

        // Then
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOnlineUsers_ShouldReturnOnlineUserDTOs() {
        // Given
        User user1 = new User("user1");
        user1.setId(1L);
        user1.setStatus(UserStatus.ONLINE);

        User user2 = new User("user2");
        user2.setId(2L);
        user2.setStatus(UserStatus.ONLINE);

        when(userRepository.findByStatus(UserStatus.ONLINE))
                .thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserDTO> result = userService.getOnlineUsers();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        assertTrue(result.stream().allMatch(dto -> "ONLINE".equals(dto.getStatus())));
        verify(userRepository).findByStatus(UserStatus.ONLINE);
    }

    @Test
    void getOnlineUsers_NoOnlineUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findByStatus(UserStatus.ONLINE)).thenReturn(Arrays.asList());

        // When
        List<UserDTO> result = userService.getOnlineUsers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findByStatus(UserStatus.ONLINE);
    }
}
