package com.neurixa.configuration;

import com.neurixa.core.port.PasswordEncoder;
import com.neurixa.core.port.UserRepository;
import com.neurixa.core.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new LoginUserUseCase(userRepository, passwordEncoder);
    }

    @Bean
    public GetUserByUsernameUseCase getUserByUsernameUseCase(UserRepository userRepository) {
        return new GetUserByUsernameUseCase(userRepository);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepository userRepository) {
        return new ListUsersUseCase(userRepository);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository) {
        return new UpdateUserUseCase(userRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }

    @Bean
    public LockUserUseCase lockUserUseCase(UserRepository userRepository) {
        return new LockUserUseCase(userRepository);
    }

    @Bean
    public UnlockUserUseCase unlockUserUseCase(UserRepository userRepository) {
        return new UnlockUserUseCase(userRepository);
    }

    @Bean
    public ResetFailedLoginUseCase resetFailedLoginUseCase(UserRepository userRepository) {
        return new ResetFailedLoginUseCase(userRepository);
    }

    @Bean
    public GetUsersUseCase getUsersUseCase(UserRepository userRepository) {
        return new GetUsersUseCase(userRepository);
    }

    @Bean
    public ChangeUserRoleUseCase changeUserRoleUseCase(UserRepository userRepository) {
        return new ChangeUserRoleUseCase(userRepository);
    }
}
