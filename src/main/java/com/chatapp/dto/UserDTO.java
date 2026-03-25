package com.chatapp.dto;

/**
 * Data-transfer object for user information.
 *
 * <p>
 * Returned by REST endpoints to expose user details
 * without leaking JPA internals.
 * </p>
 */
public class UserDTO {

    private Long id;
    private String username;
    private String status;

    // ─── Constructors ──────────────────────────────────────────────────
    public UserDTO() {
    }

    public UserDTO(Long id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
