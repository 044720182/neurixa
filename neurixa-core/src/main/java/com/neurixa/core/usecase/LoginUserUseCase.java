package com.neurixa.core.usecase;

import com.neurixa.core.domain.User;
import com.neurixa.core.exception.InvalidCredentialsException;
import com.neurixa.core.port.PasswordEncoder;
import com.neurixa.core.port.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LoginUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUserUseCase.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder cannot be null");
    }

    public User execute(String usernameOrEmail, String rawPassword) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    log.warn("event=login_failed reason=user_not_found identifier={}", usernameOrEmail);
                    return new InvalidCredentialsException("Invalid username or password");
                });

        if (user.isLocked()) {
            log.warn("event=login_failed reason=account_locked username={}", user.getUsername());
            throw new InvalidCredentialsException("Account is locked");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("event=login_failed reason=wrong_password username={} attempts={}",
                    user.getUsername(), user.getFailedLoginAttempts() + 1);
            userRepository.save(user.recordFailedLogin());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (user.getFailedLoginAttempts() > 0) {
            user = userRepository.save(user.resetFailedLogin());
        }

        log.info("event=login_success username={} role={}", user.getUsername(), user.getRole());
        return user;
    }
}
