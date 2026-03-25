package com.chatapp.model;

import com.chatapp.model.User.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructor_NoArgs_ShouldCreateUserWithDefaultStatus() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
        assertEquals(UserStatus.ONLINE, user.getStatus());
        assertNull(user.getId());
        assertNull(user.getUsername());
    }

    @Test
    void constructor_WithUsername_ShouldSetUsername() {
        // When
        User user = new User("testuser");

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals(UserStatus.ONLINE, user.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        User user = new User();

        // When
        user.setId(1L);
        user.setUsername("john");
        user.setStatus(UserStatus.OFFLINE);

        // Then
        assertEquals(1L, user.getId());
        assertEquals("john", user.getUsername());
        assertEquals(UserStatus.OFFLINE, user.getStatus());
    }

    @Test
    void setStatus_ToOnline_ShouldUpdate() {
        // Given
        User user = new User("testuser");
        user.setStatus(UserStatus.OFFLINE);

        // When
        user.setStatus(UserStatus.ONLINE);

        // Then
        assertEquals(UserStatus.ONLINE, user.getStatus());
    }

    @Test
    void setStatus_ToOffline_ShouldUpdate() {
        // Given
        User user = new User("testuser");

        // When
        user.setStatus(UserStatus.OFFLINE);

        // Then
        assertEquals(UserStatus.OFFLINE, user.getStatus());
    }

    @Test
    void userStatus_Enum_ShouldHaveCorrectValues() {
        // Then
        assertEquals(2, UserStatus.values().length);
        assertEquals("ONLINE", UserStatus.ONLINE.name());
        assertEquals("OFFLINE", UserStatus.OFFLINE.name());
    }
}
