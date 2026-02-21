package com.neurixa.configuration;

import com.neurixa.core.port.UserRepository;
import com.neurixa.core.usecase.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {
    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
        return new RegisterUserUseCase(userRepository);
    }
}
