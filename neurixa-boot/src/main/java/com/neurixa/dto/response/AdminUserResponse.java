package com.neurixa.dto.response;

import com.neurixa.core.domain.Role;

import java.time.Instant;

public record AdminUserResponse(
        String id,
        String username,
        String email,
        Role role,
        boolean locked,
        boolean emailVerified,
        int failedLoginAttempts,
        Instant createdAt,
        Instant updatedAt
) {}
