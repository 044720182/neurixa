package com.neurixa.controller;

import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.usecase.*;
import com.neurixa.dto.request.ChangeUserRoleRequest;
import com.neurixa.dto.request.UpdateUserRequest;
import com.neurixa.dto.response.AdminUserResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final LockUserUseCase lockUserUseCase;
    private final UnlockUserUseCase unlockUserUseCase;
    private final ResetFailedLoginUseCase resetFailedLoginUseCase;
    private final GetUserByUsernameUseCase getUserByUsernameUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        List<User> users = listUsersUseCase.execute();
        List<AdminUserResponse> response = users.stream().map(this::toAdminUserResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponse> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request) {
        User updatedUser = updateUserUseCase.execute(new UserId(id), request.email(), request.role());
        return ResponseEntity.ok(toAdminUserResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, Principal principal) {
        User requestingAdmin = getUserByUsernameUseCase.execute(principal.getName());
        deleteUserUseCase.execute(new UserId(id), requestingAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<AdminUserResponse> lockUser(@PathVariable String id) {
        User lockedUser = lockUserUseCase.execute(new UserId(id));
        return ResponseEntity.ok(toAdminUserResponse(lockedUser));
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<AdminUserResponse> unlockUser(@PathVariable String id) {
        User unlockedUser = unlockUserUseCase.execute(new UserId(id));
        return ResponseEntity.ok(toAdminUserResponse(unlockedUser));
    }

    @PostMapping("/{id}/reset-failed-login")
    public ResponseEntity<AdminUserResponse> resetFailedLogin(@PathVariable String id) {
        User user = resetFailedLoginUseCase.execute(new UserId(id));
        return ResponseEntity.ok(toAdminUserResponse(user));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<AdminUserResponse> changeUserRole(
            @PathVariable String id,
            @Valid @RequestBody ChangeUserRoleRequest request,
            Principal principal) {
        User requestingUser = getUserByUsernameUseCase.execute(principal.getName());
        User updatedUser = changeUserRoleUseCase.execute(new UserId(id), request.role(), requestingUser);
        return ResponseEntity.ok(toAdminUserResponse(updatedUser));
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return new AdminUserResponse(
                user.getId().getValue(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isLocked(),
                user.isEmailVerified(),
                user.getFailedLoginAttempts(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
