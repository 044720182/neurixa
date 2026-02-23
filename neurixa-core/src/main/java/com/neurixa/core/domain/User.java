package com.neurixa.core.domain;

import com.neurixa.core.exception.InvalidUserStateException;

import java.util.Objects;
import java.util.regex.Pattern;

public class User {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final String id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String role;

    public User(String id, String username, String email, String passwordHash, String role) {
        validateUsername(username);
        validateEmail(email);
        validatePasswordHash(passwordHash);
        validateRole(role);
        
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserStateException("Username cannot be null or blank");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new InvalidUserStateException("Username must be between 3 and 50 characters");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidUserStateException("Email cannot be null or blank");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserStateException("Email must be valid");
        }
    }

    private void validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidUserStateException("Password hash cannot be null or blank");
        }
    }

    private void validateRole(String role) {
        if (role == null || role.isBlank()) {
            throw new InvalidUserStateException("Role cannot be null or blank");
        }
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email);
    }

    @Override
    public String toString() {
        return "User{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               '}';
    }
}
