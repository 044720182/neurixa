package com.neurixa.core.usecase;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class UpdateUserUseCase {
    private final UserRepository userRepository;

    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public User execute(UserId id, String email, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        if (email != null && !email.equals(user.getEmail())) {
            user = user.changeEmail(email);
        }

        if (role != null && role != user.getRole()) {
            user = user.promote(role);
        }

        return userRepository.save(user);
    }
}
