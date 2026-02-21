package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.port.UserRepository;

public class RegisterUserUseCase {
    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        this.userRepository = userRepository;
    }

    public User execute(String username, String email, String passwordHash, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        User user = new User(null, username, email, passwordHash, role);
        return userRepository.save(user);
    }
}
