package com.neurixa.core.usecase;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.port.PasswordEncoder;
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
    private PasswordEncoder passwordEncoder;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = useCase.execute("john_doe", "john@example.com", "password");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(result.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        User existingUser = User.createNew("john_doe", "other@example.com", "hash", Role.USER);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> useCase.execute("john_doe", "john@example.com", "password"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists: john_doe");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        User existingUser = User.createNew("other_user", "john@example.com", "hash", Role.USER);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> useCase.execute("john_doe", "john@example.com", "password"))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists: john@example.com");
    }

    @Test
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new RegisterUserUseCase(null, passwordEncoder))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("UserRepository cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenPasswordEncoderIsNull() {
        assertThatThrownBy(() -> new RegisterUserUseCase(userRepository, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PasswordEncoder cannot be null");
    }
}
