package com.chatapp.service;

import com.chatapp.dto.UserDTO;
import com.chatapp.model.User;
import com.chatapp.model.User.UserStatus;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user lifecycle management (register, connect, disconnect)
 * and online-user queries.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ─── Registration / Connection ─────────────────────────────────────

    /**
     * Register a new user or re-activate an existing one.
     * If the username already exists, simply set them ONLINE.
     *
     * @param username the display name chosen by the user
     * @return DTO with user details
     */
    @Transactional
    public UserDTO connectUser(String username) {
        User user = userRepository.findByUsername(username)
                .map(existing -> {
                    existing.setStatus(UserStatus.ONLINE);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = new User(username);
                    newUser.setStatus(UserStatus.ONLINE);
                    return userRepository.save(newUser);
                });
        return toDTO(user);
    }

    /**
     * Mark a user as OFFLINE when they disconnect.
     *
     * @param username the display name of the disconnecting user
     */
    @Transactional
    public void disconnectUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus(UserStatus.OFFLINE);
            userRepository.save(user);
        });
    }

    // ─── Queries ───────────────────────────────────────────────────────

    /**
     * Return a list of all currently online users.
     */
    public List<UserDTO> getOnlineUsers() {
        return userRepository.findByStatus(UserStatus.ONLINE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Return a list of ALL registered users (online + offline).
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Return the total number of registered users.
     */
    public long getUserCount() {
        return userRepository.count();
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    /** Convert a domain entity to a DTO. */
    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getStatus().name());
    }
}
