package com.neurixa.core.usecase;

import com.neurixa.core.domain.Page;
import com.neurixa.core.domain.User;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class GetUsersUseCase {
    private final UserRepository userRepository;

    public GetUsersUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public Page<User> execute(String search, String role, Boolean locked, int page, int size, String sortBy, String sortDirection) {
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20; // Default page size
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }
        if (sortDirection == null || sortDirection.isBlank()) {
            sortDirection = "desc";
        }
        
        return userRepository.findAllWithFilters(search, role, locked, page, size, sortBy, sortDirection);
    }
}
