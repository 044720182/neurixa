package com.neurixa.core.domain;

import com.neurixa.core.exception.InvalidUserStateException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public final class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserId id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean locked;
    private final boolean emailVerified;
    private final int failedLoginAttempts;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(
            UserId id,
            String username,
            String email,
            String passwordHash,
            Role role,
            boolean locked,
            boolean emailVerified,
            int failedLoginAttempts,
            Instant createdAt,
            Instant updatedAt) {

        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.username = validateUsername(username);
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.failedLoginAttempts = validateFailedLoginAttempts(failedLoginAttempts);
        this.locked = locked;
        this.emailVerified = emailVerified;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    public static User createNew(String username, String email, String passwordHash, Role role) {
        UserId id = new UserId(UUID.randomUUID().toString());
        Instant now = Instant.now();
        return new User(id, username, email, passwordHash, role, false, false, 0, now, now);
    }

    public static User from(
            UserId id,
            String username,
            String email,
            String passwordHash,
            Role role,
            boolean locked,
            boolean emailVerified,
            int failedLoginAttempts,
            Instant createdAt,
            Instant updatedAt) {
        return new User(id, username, email, passwordHash, role, locked, emailVerified, failedLoginAttempts, createdAt, updatedAt);
    }

    public User changeEmail(String newEmail) {
        return new User(
                this.id,
                this.username,
                newEmail,
                this.passwordHash,
                this.role,
                this.locked,
                false, // Email change requires new verification
                this.failedLoginAttempts,
                this.createdAt,
                Instant.now()
        );
    }

    public User changePassword(String newPasswordHash) {
        return new User(
                this.id,
                this.username,
                this.email,
                newPasswordHash,
                this.role,
                this.locked,
                this.emailVerified,
                this.failedLoginAttempts,
                this.createdAt,
                Instant.now()
        );
    }

    public User promote(Role newRole) {
        if (this.role == Role.SUPER_ADMIN) {
            throw new InvalidUserStateException("SUPER_ADMIN cannot be demoted.");
        }
        if (this.locked) {
            throw new InvalidUserStateException("Locked user cannot be promoted.");
        }
        return new User(
                this.id,
                this.username,
                this.email,
                this.passwordHash,
                newRole,
                this.locked,
                this.emailVerified,
                this.failedLoginAttempts,
                this.createdAt,
                Instant.now()
        );
    }

    public User lock() {
        return new User(
                this.id,
                this.username,
                this.email,
                this.passwordHash,
                this.role,
                true,
                this.emailVerified,
                this.failedLoginAttempts,
                this.createdAt,
                Instant.now()
        );
    }

    public User unlock() {
        return new User(
                this.id,
                this.username,
                this.email,
                this.passwordHash,
                this.role,
                false,
                this.emailVerified,
                0, // Also reset failed attempts
                this.createdAt,
                Instant.now()
        );
    }

    public User verifyEmail() {
        return new User(
                this.id,
                this.username,
                this.email,
                this.passwordHash,
                this.role,
                this.locked,
                true,
                this.failedLoginAttempts,
                this.createdAt,
                Instant.now()
        );
    }

    public User recordFailedLogin() {
        int newAttemptCount = this.failedLoginAttempts + 1;
        boolean shouldLock = newAttemptCount >= MAX_FAILED_ATTEMPTS;
        return new User(
                this.id,
                this.username,
                this.email,
                this.passwordHash,
                this.role,
                shouldLock || this.locked,
                this.emailVerified,
                newAttemptCount,
                this.createdAt,
                Instant.now()
        );
    }

    public User resetFailedLogin() {
        return new User(
                this.id,
                this.username,
                this.email,
                this.passwordHash,
                this.role,
                this.locked,
                this.emailVerified,
                0,
                this.createdAt,
                Instant.now()
        );
    }

    // --- Getters ---

    public UserId getId() {
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

    public Role getRole() {
        return role;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() { 
        return updatedAt;
    }

    // --- Validation ---

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserStateException("Username cannot be null or blank.");
        }
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            throw new InvalidUserStateException(
                    "Username must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters.");
        }
        return username;
    }

    private String validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserStateException("Invalid email format.");
        }
        return email;
    }

    private String validatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvalidUserStateException("Password hash cannot be null or blank.");
        }
        return passwordHash;
    }

    private int validateFailedLoginAttempts(int attempts) {
        if (attempts < 0) {
            throw new InvalidUserStateException("Failed login attempts cannot be negative.");
        }
        return attempts;
    }

    // --- Equality and HashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- toString ---

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", locked=" + locked +
                ", emailVerified=" + emailVerified +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
