package com.neurixa.adapter.persistence;

import com.neurixa.core.domain.Role;
import com.neurixa.core.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MongoUserRepositoryTest {


    @InjectMocks
    private MongoUserRepository mongoUserRepository;

    @Test
    void toDomain_shouldHandleNullCreatedAt() {
        // Given: A UserDocument with null createdAt and updatedAt (legacy data)
        UserDocument document = UserDocument.builder()
                .id("test-id")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .role(Role.USER)
                .locked(false)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .createdAt(null)  // This would cause the original error
                .updatedAt(null)  // This would cause the original error
                .build();

        // When: Converting to domain object
        // This should not throw "CreatedAt cannot be null" exception anymore
        User user = mongoUserRepository.toDomain(document);

        // Then: User object is created with default timestamps
        assertNotNull(user);
        assertNotNull(user.getCreatedAt(), "CreatedAt should not be null even if document has null createdAt");
        assertNotNull(user.getUpdatedAt(), "UpdatedAt should not be null even if document has null updatedAt");
        assertEquals("test-id", user.getId().getValue());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(Role.USER, user.getRole());
        assertFalse(user.isLocked());
        assertTrue(user.isEmailVerified());
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void toDomain_shouldPreserveExistingTimestamps() {
        // Given: A UserDocument with existing timestamps
        Instant fixedTime = Instant.parse("2024-01-01T12:00:00Z");
        UserDocument document = UserDocument.builder()
                .id("test-id")
                .username("testuser")
                .email("test@example.com")
                .password("hashedpassword")
                .role(Role.USER)
                .locked(false)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();

        // When: Converting to domain object
        User user = mongoUserRepository.toDomain(document);

        // Then: Timestamps are preserved
        assertNotNull(user);
        assertEquals(fixedTime, user.getCreatedAt());
        assertEquals(fixedTime, user.getUpdatedAt());
    }
}
