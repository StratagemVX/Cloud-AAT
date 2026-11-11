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
        String normalized = username != null ? username.trim() : "";
        if (normalized.length() < 2 || normalized.length() > 30 || "ChatBot".equalsIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Username must be between 2 and 30 characters and not reserved");
        }
        return ResponseEntity.ok(userService.connectUser(normalized));
    }

    /**
     * POST /api/users/disconnect — mark a user as offline.
     *
     * @param username the display name
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect(@RequestParam String username) {
        String normalized = username != null ? username.trim() : "";
        if (!normalized.isEmpty() && normalized.length() <= 30) {
            userService.disconnectUser(normalized);
        }
        return ResponseEntity.ok().build();
    }
}
