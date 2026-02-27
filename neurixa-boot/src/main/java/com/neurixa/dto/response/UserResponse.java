package com.neurixa.dto.response;

import com.neurixa.core.domain.Role;

public record UserResponse(
        String id,
        String username,
        String email,
        Role role
) {}
