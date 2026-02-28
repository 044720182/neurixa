package com.neurixa.controller;

import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.usecase.DeleteUserUseCase;
import com.neurixa.core.usecase.GetUserByUsernameUseCase;
import com.neurixa.core.usecase.GetUsersUseCase;
import com.neurixa.dto.response.MessageResponse;
import com.neurixa.dto.response.PageResponse;
import com.neurixa.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserByUsernameUseCase getUserByUsernameUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = getUserByUsernameUseCase.execute(principal.getName());
        return ResponseEntity.ok(toUserResponse(user));
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        com.neurixa.core.domain.Page<User> userPage =
                getUsersUseCase.execute(search, role, locked, page, size, sortBy, sortDirection);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::toUserResponse)
                .toList();

        return ResponseEntity.ok(new PageResponse<>(
                userResponses,
                userPage.getPageNumber(),
                userPage.getPageSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.hasNext(),
                userPage.hasPrevious()
        ));
    }

    /**
     * Deletes a user account.
     *
     * <p>Authorization rules:
     * <ul>
     *   <li>ADMIN / SUPER_ADMIN: can delete any non-SUPER_ADMIN user.</li>
     *   <li>USER: can only delete their own account (self-delete).</li>
     * </ul>
     *
     * <p>Additional safeguards enforced in the use-case:
     * <ul>
     *   <li>SUPER_ADMIN accounts cannot be deleted.</li>
     *   <li>The last ADMIN cannot be deleted unless a SUPER_ADMIN is present.</li>
     * </ul>
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteUser(
            @PathVariable String id,
            Principal principal) {

        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User requestingUser = getUserByUsernameUseCase.execute(principal.getName());
        deleteUserUseCase.execute(new UserId(id), requestingUser);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId().getValue(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
