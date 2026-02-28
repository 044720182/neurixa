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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangeUserRoleUseCaseTest {

    private UserRepository userRepository;
    private ChangeUserRoleUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new ChangeUserRoleUseCase(userRepository);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void superAdminShouldPromoteUserToAdmin() {
        User superAdmin = User.createNew("super_admin", "super@example.com", "hash", Role.SUPER_ADMIN);
        User target = User.createNew("user", "user@example.com", "hash", Role.USER);
        User expectedUpdated = target.promote(Role.ADMIN);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(expectedUpdated)).thenReturn(expectedUpdated);

        User result = useCase.execute(target.getId(), Role.ADMIN, superAdmin);

        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(expectedUpdated);
    }

    @Test
    void superAdminShouldPromoteUserToSuperAdmin() {
        User superAdmin = User.createNew("super_admin", "super@example.com", "hash", Role.SUPER_ADMIN);
        User target = User.createNew("user", "user@example.com", "hash", Role.USER);
        User expectedUpdated = target.promote(Role.SUPER_ADMIN);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(expectedUpdated)).thenReturn(expectedUpdated);

        User result = useCase.execute(target.getId(), Role.SUPER_ADMIN, superAdmin);

        assertThat(result.getRole()).isEqualTo(Role.SUPER_ADMIN);
        verify(userRepository).save(expectedUpdated);
    }

    @Test
    void adminShouldPromoteUserToAdmin() {
        User admin = User.createNew("admin", "admin@example.com", "hash", Role.ADMIN);
        User target = User.createNew("user", "user@example.com", "hash", Role.USER);
        User expectedUpdated = target.promote(Role.ADMIN);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(expectedUpdated)).thenReturn(expectedUpdated);

        User result = useCase.execute(target.getId(), Role.ADMIN, admin);

        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(expectedUpdated);
    }

    @Test
    void adminShouldDemoteUserToUser() {
        User admin = User.createNew("admin", "admin@example.com", "hash", Role.ADMIN);
        User target = User.createNew("user", "user@example.com", "hash", Role.ADMIN);
        User expectedUpdated = target.promote(Role.USER);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));
        when(userRepository.save(expectedUpdated)).thenReturn(expectedUpdated);

        User result = useCase.execute(target.getId(), Role.USER, admin);

        assertThat(result.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(expectedUpdated);
    }

    // -------------------------------------------------------------------------
    // Authorization failures
    // -------------------------------------------------------------------------

    @Test
    void adminShouldNotPromoteToSuperAdmin() {
        User admin = User.createNew("admin", "admin@example.com", "hash", Role.ADMIN);
        User target = User.createNew("user", "user@example.com", "hash", Role.USER);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> useCase.execute(target.getId(), Role.SUPER_ADMIN, admin))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessage("Insufficient permissions to change role");
    }

    @Test
    void userShouldNotChangeRoles() {
        User user = User.createNew("user", "user@example.com", "hash", Role.USER);
        User target = User.createNew("target", "target@example.com", "hash", Role.USER);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> useCase.execute(target.getId(), Role.ADMIN, user))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessage("Insufficient permissions to change role");
    }

    // -------------------------------------------------------------------------
    // Domain logic failures (delegated to User.promote)
    // -------------------------------------------------------------------------

    @Test
    void shouldNotDemoteSuperAdmin() {
        User superAdmin = User.createNew("super_admin", "super@example.com", "hash", Role.SUPER_ADMIN);
        User target = User.createNew("target", "target@example.com", "hash", Role.SUPER_ADMIN);

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> useCase.execute(target.getId(), Role.ADMIN, superAdmin))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessage("SUPER_ADMIN cannot be demoted.");
    }

    @Test
    void shouldNotPromoteLockedUser() {
        User superAdmin = User.createNew("super_admin", "super@example.com", "hash", Role.SUPER_ADMIN);
        User target = User.createNew("user", "user@example.com", "hash", Role.USER).lock();

        when(userRepository.findById(target.getId())).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> useCase.execute(target.getId(), Role.ADMIN, superAdmin))
                .isInstanceOf(InvalidUserStateException.class)
                .hasMessage("Locked user cannot be promoted.");
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowWhenUserNotFound() {
        User superAdmin = User.createNew("super_admin", "super@example.com", "hash", Role.SUPER_ADMIN);
        UserId nonExistentId = new UserId("non-existent");

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(nonExistentId, Role.ADMIN, superAdmin))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + nonExistentId);
    }
}
