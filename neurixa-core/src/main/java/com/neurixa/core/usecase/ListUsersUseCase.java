package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.port.UserRepository;

import java.util.List;
import java.util.Objects;

public class ListUsersUseCase {
    private final UserRepository userRepository;

    public ListUsersUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public List<User> execute() {
        return userRepository.findAll();
    }
}
