package com.neurixa.core.usecase;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.port.PasswordEncoder;
import com.neurixa.core.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder cannot be null");
    }

    public User execute(String username, String email, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(u -> {
            log.warn("event=register_failed reason=duplicate_username username={}", username);
            throw new UserAlreadyExistsException(UserAlreadyExistsException.Field.USERNAME,
                    "Username already exists: " + username);
        });

        userRepository.findByEmail(email).ifPresent(u -> {
            log.warn("event=register_failed reason=duplicate_email username={}", username);
            throw new UserAlreadyExistsException(UserAlreadyExistsException.Field.EMAIL,
                    "Email already exists: " + email);
        });

        String passwordHash = passwordEncoder.encode(rawPassword);
        User newUser = User.createNew(username, email, passwordHash, Role.USER);
        User saved = userRepository.save(newUser);
        log.info("event=user_registered username={} role={}", saved.getUsername(), saved.getRole());
        return saved;
    }
}
