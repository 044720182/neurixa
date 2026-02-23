package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class GetUserByUsernameUseCase {
    private final UserRepository userRepository;

    public GetUserByUsernameUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public User execute(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
}
