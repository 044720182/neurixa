package com.neurixa.core.usecase;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import com.neurixa.core.domain.UserId;
import com.neurixa.core.exception.InvalidUserStateException;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteUserUseCaseTest {

    private UserRepository userRepository;
    private DeleteUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new DeleteUserUseCase(userRepository);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void adminShouldDeleteAnotherUser() {
        User admin = User.createNew("admin_user", "admin@example.com", "hash", Role.ADMIN);
        User target = User.createNew("regular_user", "user@example.com", "hash", Role.USER);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        useCase.execute(target.getId(), admin);

        verify(userRepository).deleteById(target.getId());
    }

    @Test
    void userShouldDeleteTheirOwnAccount() {
        User user = User.createNew("self_user", "self@example.com", "hash", Role.USER);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        useCase.execute(user.getId(), user);

        verify(userRepository).deleteById(user.getId());
    }

    @Test
    void adminShouldDeleteAdminWhenMultipleAdminsExist() {
        User requestingAdmin = User.createNew("admin1", "admin1@example.com", "hash", Role.ADMIN);
        User targetAdmin = User.createNew("admin2", "admin2@example.com", "hash", Role.ADMIN);

        when(userRepository.findById(targetAdmin.getId())).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRole(Role.ADMIN.name())).thenReturn(2L);
        when(userRepository.countByRole(Role.SUPER_ADMIN.name())).thenReturn(0L);

        useCase.execute(targetAdmin.getId(), requestingAdmin);

        verify(userRepository).deleteById(targetAdmin.getId());
    }

    @Test
    void adminShouldDeleteLastAdminWhenSuperAdminExists() {
        User superAdmin = User.createNew("super", "super@example.com", "hash", Role.SUPER_ADMIN);
        User targetAdmin = User.createNew("last_admin", "admin@example.com", "hash", Role.ADMIN);

        when(userRepository.findById(targetAdmin.getId())).thenReturn(Optional.of(targetAdmin));
        when(userRepository.countByRole(Role.ADMIN.name())).thenReturn(1L);
        when(userRepository.countByRole(Role.SUPER_ADMIN.name())).thenReturn(1L);

        useCase.execute(targetAdmin.getId(), superAdmin);

        verify(userRepository).deleteById(targetAdmin.getId());
    }

    // -------------------------------------------------------------------------
    // Guard: user not found
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowUserNotFoundWhenTargetDoesNotExist() {
        User admin = User.createNew("admin_user", "admin@example.com", "hash", Role.ADMIN);
        UserId missingId = new UserId("non-existent-id");

        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(missingId, admin))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("non-existent-id");

        verify(userRepository, never()).deleteById(missingId);
    }

    // -------------------------------------------------------------------------
    // Guard: SUPER_ADMIN cannot be deleted
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowWhenDeletingSuperAdmin() {
        User admin = User.createNew("admin_user", "admin@example.com", "hash", Role.ADMIN);
        User superAdmin = User.createNew("super_admin", "super@example.com", "hash", Role.SUPER_ADMIN);

        when(userRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));

        assertThatThrownBy(() -> useCase.execute(superAdmin.getId(), admin))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("SUPER_ADMIN accounts cannot be deleted");

        verify(userRepository, never()).deleteById(superAdmin.getId());
    }

    // -------------------------------------------------------------------------
    // Guard: USER may not delete another user
    // -------------------------------------------------------------------------

    @Test
    void userShouldNotDeleteAnotherUsersAccount() {
        User requestingUser = User.createNew("user_a", "a@example.com", "hash", Role.USER);
        User targetUser = User.createNew("user_b", "b@example.com", "hash", Role.USER);

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        assertThatThrownBy(() -> useCase.execute(targetUser.getId(), requestingUser))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Users may only delete their own account");

        verify(userRepository, never()).deleteById(targetUser.getId());
    }

    // -------------------------------------------------------------------------
    // Guard: last ADMIN cannot be deleted without a fallback SUPER_ADMIN
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowWhenDeletingLastAdminWithNoSuperAdmin() {
        User admin = User.createNew("admin_user", "admin@example.com", "hash", Role.ADMIN);

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(Role.ADMIN.name())).thenReturn(1L);
        when(userRepository.countByRole(Role.SUPER_ADMIN.name())).thenReturn(0L);

        assertThatThrownBy(() -> useCase.execute(admin.getId(), admin))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessageContaining("Cannot delete the last ADMIN account");

        verify(userRepository, never()).deleteById(admin.getId());
    }

    // -------------------------------------------------------------------------
    // Guard: null inputs
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowWhenTargetIdIsNull() {
        User admin = User.createNew("admin_user", "admin@example.com", "hash", Role.ADMIN);

        assertThatThrownBy(() -> useCase.execute(null, admin))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Target user ID cannot be null");
    }

    @Test
    void shouldThrowWhenRequestingUserIsNull() {
        UserId targetId = new UserId("some-id");

        assertThatThrownBy(() -> useCase.execute(targetId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Requesting user cannot be null");
    }

    @Test
    void shouldThrowWhenUserRepositoryIsNull() {
        assertThatThrownBy(() -> new DeleteUserUseCase(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("UserRepository cannot be null");
    }
}

