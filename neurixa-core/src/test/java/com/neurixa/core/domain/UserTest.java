package com.neurixa.core.domain;

import com.neurixa.core.exception.InvalidUserStateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateValidUser() {
        User user = new User("1", "john_doe", "john@example.com", "hashedPassword", "USER");

        assertThat(user.getId()).isEqualTo("1");
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(user.getRole()).isEqualTo("USER");
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        assertThatThrownBy(() -> new User("1", null, "john@example.com", "hashedPassword", "USER"))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Username cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsBlank() {
        assertThatThrownBy(() -> new User("1", "  ", "john@example.com", "hashedPassword", "USER"))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Username cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenUsernameTooShort() {
        assertThatThrownBy(() -> new User("1", "ab", "john@example.com", "hashedPassword", "USER"))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Username must be between 3 and 50 characters");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThatThrownBy(() -> new User("1", "john_doe", null, "hashedPassword", "USER"))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Email cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThatThrownBy(() -> new User("1", "john_doe", "invalid-email", "hashedPassword", "USER"))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Email must be valid");
    }

    @Test
    void shouldThrowExceptionWhenPasswordHashIsNull() {
        assertThatThrownBy(() -> new User("1", "john_doe", "john@example.com", null, "USER"))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Password hash cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {
        assertThatThrownBy(() -> new User("1", "john_doe", "john@example.com", "hashedPassword", null))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Role cannot be null or blank");
    }

    @Test
    void shouldBeEqualWhenSameIdUsernameAndEmail() {
        User user1 = new User("1", "john_doe", "john@example.com", "hash1", "USER");
        User user2 = new User("1", "john_doe", "john@example.com", "hash2", "ADMIN");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentUsername() {
        User user1 = new User("1", "john_doe", "john@example.com", "hash", "USER");
        User user2 = new User("1", "jane_doe", "john@example.com", "hash", "USER");

        assertThat(user1).isNotEqualTo(user2);
    }
}
