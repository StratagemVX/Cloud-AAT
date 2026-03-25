package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a chat user.
 *
 * <p>
 * Tracks username uniqueness, online/offline status, and the
 * timestamp the user first joined the application.
 * </p>
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique display name chosen at login. */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Current presence status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ONLINE;

    /** Timestamp recorded when the user first registers. */
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // ─── Enum ──────────────────────────────────────────────────────────
    public enum UserStatus {
        ONLINE, OFFLINE
    }

    // ─── Lifecycle callback ────────────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    // ─── Constructors ──────────────────────────────────────────────────
    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
