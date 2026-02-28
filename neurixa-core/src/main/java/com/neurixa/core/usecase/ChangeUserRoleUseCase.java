package com.neurixa.core.usecase;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.exception.InvalidUserStateException;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class ChangeUserRoleUseCase {
    private final UserRepository userRepository;

    public ChangeUserRoleUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    public User execute(UserId id, Role newRole, User requestingUser) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

        // Authorization checks
        if (!canChangeRole(requestingUser.getRole(), targetUser.getRole(), newRole)) {
            throw new InvalidUserStateException("Insufficient permissions to change role");
        }

        // Domain logic: promote method handles SUPER_ADMIN demotion and locked user promotion
        return userRepository.save(targetUser.promote(newRole));
    }

    private boolean canChangeRole(Role requesterRole, Role currentRole, Role newRole) {
        // SUPER_ADMIN can do anything (except demoting other SUPER_ADMINs, but that's handled in domain)
        if (requesterRole == Role.SUPER_ADMIN) {
            return true;
        }

        // ADMIN can promote/demote to USER or ADMIN, but not to SUPER_ADMIN
        if (requesterRole == Role.ADMIN) {
            return newRole == Role.USER || newRole == Role.ADMIN;
        }

        // USER cannot change roles
        return false;
    }
}
