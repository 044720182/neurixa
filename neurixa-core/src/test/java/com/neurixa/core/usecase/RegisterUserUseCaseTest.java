package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegisterUserUseCaseTest {

    private UserRepository userRepository;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new RegisterUserUseCase(userRepository);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User("1", user.getUsername(), user.getEmail(), user.getPasswordHash(), user.getRole());
        });

        User result = useCase.execute("john_doe", "john@example.com", "hashedPassword", "USER");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        User existingUser = new User("1", "john_doe", "other@example.com", "hash", "USER");
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> useCase.execute("john_doe", "john@example.com", "hashedPassword", "USER"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists: john_doe");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        User existingUser = new User("1", "other_user", "john@example.com", "hash", "USER");
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> useCase.execute("john_doe", "john@example.com", "hashedPassword", "USER"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists: john@example.com");
    }

    @Test
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new RegisterUserUseCase(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserRepository cannot be null");
    }
}
