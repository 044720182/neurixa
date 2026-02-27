package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class LockUserUseCase {
    private final UserRepository userRepository;

    public LockUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public User execute(UserId id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return userRepository.save(user.lock());
    }
}
