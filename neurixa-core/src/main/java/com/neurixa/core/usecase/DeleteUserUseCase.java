package com.neurixa.core.usecase;

import com.neurixa.core.domain.UserId;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class DeleteUserUseCase {
    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public void execute(UserId id) {
        userRepository.deleteById(id);
    }
}
