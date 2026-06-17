package com.sms.auth.model.dto;

import com.sms.auth.model.User;

/**
 * Response body for /api/auth/register and /api/auth/login
 */
public class AuthResponse {

    private String token;
    private Long id;
    private String email;
    private String fullName;
    private User.Role role;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String email, String fullName, User.Role role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private Long id;
        private String email;
        private String fullName;
        private User.Role role;

        public Builder token(String token)           { this.token = token; return this; }
        public Builder id(Long id)                   { this.id = id; return this; }
        public Builder email(String email)           { this.email = email; return this; }
        public Builder fullName(String fullName)     { this.fullName = fullName; return this; }
        public Builder role(User.Role role)          { this.role = role; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, id, email, fullName, role);
        }
    }

    public String getToken()                     { return token; }
    public void setToken(String token)           { this.token = token; }

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getFullName()                  { return fullName; }
    public void setFullName(String fullName)     { this.fullName = fullName; }

    public User.Role getRole()                   { return role; }
    public void setRole(User.Role role)          { this.role = role; }
}
