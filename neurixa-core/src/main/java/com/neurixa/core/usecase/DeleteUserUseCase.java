package com.neurixa.core.usecase;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.exception.InvalidUserStateException;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.port.UserRepository;

import java.util.Objects;

public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public DeleteUserUseCase(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
    }

    /**
     * Deletes a user identified by {@code targetId}.
     *
     * <p>Safety rules enforced:
     * <ul>
     *   <li>The target user must exist.</li>
     *   <li>{@code SUPER_ADMIN} accounts can never be deleted.</li>
     *   <li>If the target is the only remaining {@code ADMIN}, deletion is blocked to prevent
     *       a lock-out situation (unless a {@code SUPER_ADMIN} is also present).</li>
     *   <li>A regular {@code USER} may only delete their own account (self-delete).</li>
     * </ul>
     *
     * @param targetId       the ID of the user to delete
     * @param requestingUser the user performing the deletion
     */
    public void execute(UserId targetId, User requestingUser) {
        Objects.requireNonNull(targetId, "Target user ID cannot be null");
        Objects.requireNonNull(requestingUser, "Requesting user cannot be null");

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + targetId.getValue()));

        // SUPER_ADMIN accounts are immutable — they cannot be deleted
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new InvalidUserStateException("SUPER_ADMIN accounts cannot be deleted.");
        }

        // A non-admin user may only delete their own account
        if (requestingUser.getRole() == Role.USER
                && !requestingUser.getId().equals(targetId)) {
            throw new InvalidUserStateException("Users may only delete their own account.");
        }

        // Prevent removing the last ADMIN (a SUPER_ADMIN is always present, so we guard only when
        // no SUPER_ADMIN exists and the target is the sole remaining ADMIN)
        if (target.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN.name());
            long superAdminCount = userRepository.countByRole(Role.SUPER_ADMIN.name());
            if (adminCount <= 1 && superAdminCount == 0) {
                throw new InvalidUserStateException(
                        "Cannot delete the last ADMIN account. Promote another user first.");
            }
        }

        userRepository.deleteById(targetId);
    }
}
