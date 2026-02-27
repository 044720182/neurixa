package com.neurixa.controller;

import com.neurixa.core.domain.User;
import com.neurixa.core.usecase.GetUserByUsernameUseCase;
import com.neurixa.core.usecase.GetUsersUseCase;
import com.neurixa.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetUserByUsernameUseCase getUserByUsernameUseCase;
    private final GetUsersUseCase getUsersUseCase;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = principal.getName();
        User user = getUserByUsernameUseCase.execute(username);
        UserResponse response = new UserResponse(
                user.getId().getValue(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<com.neurixa.dto.response.PageResponse<UserResponse>> getUsers(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String role,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean locked,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "createdAt") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "desc") String sortDirection
    ) {
        com.neurixa.core.domain.Page<User> userPage = getUsersUseCase.execute(
                search, role, locked, page, size, sortBy, sortDirection
        );

        java.util.List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> new UserResponse(
                        user.getId().getValue(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();

        com.neurixa.dto.response.PageResponse<UserResponse> response =
                new com.neurixa.dto.response.PageResponse<>(
                        userResponses,
                        userPage.getPageNumber(),
                        userPage.getPageSize(),
                        userPage.getTotalElements(),
                        userPage.getTotalPages(),
                        userPage.hasNext(),
                        userPage.hasPrevious()
                );

        return ResponseEntity.ok(response);
    }
}
