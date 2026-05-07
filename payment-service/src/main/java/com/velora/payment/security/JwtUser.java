package com.velora.payment.security;

public class JwtUser {
    private final Long userId;
    private final String username;
    private final String email;
    private final String role;

    public JwtUser(Long userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
