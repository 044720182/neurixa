package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.exception.InvalidCredentialsException;
import com.neurixa.core.port.PasswordEncoder;
import com.neurixa.core.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginUserUseCaseTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private LoginUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        useCase = new LoginUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User user = new User("1", "john_doe", "john@example.com", "hashedPassword", "USER");
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        User result = useCase.execute("john_doe", "password");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("unknown", "password"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        User user = new User("1", "john_doe", "john@example.com", "hashedPassword", "USER");
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("john_doe", "wrongPassword"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new LoginUserUseCase(null, passwordEncoder))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("UserRepository cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenPasswordEncoderIsNull() {
        assertThatThrownBy(() -> new LoginUserUseCase(userRepository, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("PasswordEncoder cannot be null");
    }
}
