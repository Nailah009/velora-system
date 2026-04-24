package com.velora.auth.dto;

public class AuthResponse {
    private String tokenType = "Bearer";
    private String token;
    private String username;
    private String email;
    private String role;
    private long expiresIn;

    public AuthResponse(String token, String username, String email, String role, long expiresIn) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getTokenType() { return tokenType; }
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public long getExpiresIn() { return expiresIn; }
}
