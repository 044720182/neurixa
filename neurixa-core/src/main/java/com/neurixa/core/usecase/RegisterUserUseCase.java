package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.port.PasswordEncoder;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder cannot be null");
    }

    public User execute(String username, String email, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = new User(null, username, email, passwordHash, role);
        return userRepository.save(user);
    }
}
