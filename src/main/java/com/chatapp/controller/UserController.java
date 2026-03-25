package com.chatapp.controller;

import com.chatapp.dto.UserDTO;
import com.chatapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations.
 *
 * <p>
 * Provides endpoints the frontend calls to query online users
 * and to trigger connect/disconnect actions.
 * </p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users — returns all registered users (online + offline).
     */
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * GET /api/users/online — returns all currently online users.
     */
    @GetMapping("/online")
    public List<UserDTO> getOnlineUsers() {
        return userService.getOnlineUsers();
    }

    /**
     * GET /api/users/count — returns the total number of registered users.
     */
    @GetMapping("/count")
    public java.util.Map<String, Long> getUserCount() {
        return java.util.Map.of("count", userService.getUserCount());
    }

    /**
     * POST /api/users/connect — register or reconnect a user.
     *
     * @param username the display name
     * @return the connected user DTO
     */
    @PostMapping("/connect")
    public ResponseEntity<UserDTO> connect(@RequestParam String username) {
        return ResponseEntity.ok(userService.connectUser(username));
    }

    /**
     * POST /api/users/disconnect — mark a user as offline.
     *
     * @param username the display name
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect(@RequestParam String username) {
        userService.disconnectUser(username);
        return ResponseEntity.ok().build();
    }
}
