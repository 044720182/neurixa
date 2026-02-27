package com.neurixa.core.domain;

import com.neurixa.core.exception.InvalidUserStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateValidUser() {
        User user = User.createNew("john_doe", "john@example.com", "hashedPassword", Role.USER);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        assertThatThrownBy(() -> User.createNew(null, "john@example.com", "hashedPassword", Role.USER))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Username cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsBlank() {
        assertThatThrownBy(() -> User.createNew("  ", "john@example.com", "hashedPassword", Role.USER))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Username cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenUsernameTooShort() {
        assertThatThrownBy(() -> User.createNew("ab", "john@example.com", "hashedPassword", Role.USER))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Username must be between 3 and 50 characters");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThatThrownBy(() -> User.createNew("john_doe", null, "hashedPassword", Role.USER))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThatThrownBy(() -> User.createNew("john_doe", "invalid-email", "hashedPassword", Role.USER))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    void shouldThrowExceptionWhenPasswordHashIsNull() {
        assertThatThrownBy(() -> User.createNew("john_doe", "john@example.com", null, Role.USER))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Password hash cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {
        assertThatThrownBy(() -> User.createNew("john_doe", "john@example.com", "hashedPassword", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Role cannot be null");
    }

    @Test
    void shouldBeEqualWhenSameId() {
        User user1 = User.createNew("john_doe", "john@example.com", "hash1", Role.USER);
        // Reconstruct with same ID
        User user2 = User.from(
                user1.getId(), "john_doe", "john@example.com", "hash2", Role.ADMIN,
                false, false, 0, user1.getCreatedAt(), user1.getUpdatedAt()
        );

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        User user1 = User.createNew("john_doe", "john@example.com", "hash", Role.USER);
        User user2 = User.createNew("john_doe", "john@example.com", "hash", Role.USER);

        assertThat(user1).isNotEqualTo(user2);
    }
}
